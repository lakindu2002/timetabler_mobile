package com.cb007787.timetabler.recyclers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.cb007787.timetabler.R;
import com.cb007787.timetabler.model.SuccessResponseAPI;
import com.cb007787.timetabler.model.User;
import com.cb007787.timetabler.service.APIConfigurer;
import com.cb007787.timetabler.service.PreferenceInformation;
import com.cb007787.timetabler.service.SharedPreferenceService;
import com.cb007787.timetabler.service.UserService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class UserRecycler extends RecyclerView.Adapter<UserRecycler.ViewHolder> {
    private final Context theContext;
    private List<User> userList;
    private SimpleDateFormat dateFormat;
    private String userRole;
    private UserService userService;

    public UserRecycler(Context theContext, String userRole) {
        this.theContext = theContext;
        this.userRole = userRole;
        this.userService = APIConfigurer.getApiConfigurer().getUserService();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //create a view for each user
        LayoutInflater theInflater = LayoutInflater.from(theContext);
        View inflatedView = theInflater.inflate(R.layout.user_card, parent, false);
        return new ViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserRecycler.ViewHolder holder, int position) {
        //bind each user to each card view inflated.
        User theUser = userList.get(position);//retrieve user at the position of the recycler;
        holder.getUsername().setText(theUser.getUsername());
        holder.getFullName().setText(String.format("%s %s", theUser.getFirstName(), theUser.getLastName()));
        holder.getEmailAddress().setText(theUser.getEmailAddress());
        holder.getContactNumber().setText(theUser.getContactNumber());
        holder.getDateOfBirth().setText(dateFormat.format(theUser.getDateOfBirth()));
        holder.getMemberSince().setText(String.format("Member Since: %s", dateFormat.format(theUser.getMemberSince())));

        //anchor the popup menu to the card itself
        PopupMenu theMoreOption = new PopupMenu(theContext, holder.getMore());
        theMoreOption.inflate(R.menu.user_popup);

        Menu theInflatedMenu = theMoreOption.getMenu();
        if (userRole.equalsIgnoreCase("academic administrator")) {
            //do not show the delete button to the academic administrator as they cannot delete the user information
            theInflatedMenu.removeItem(R.id.delete_click);
        }

        theMoreOption.setOnMenuItemClickListener(item -> {
            //handle item clicks
            if (item.getItemId() == R.id.call_click) {
                //user click call
                Intent theCallIntent = new Intent(Intent.ACTION_CALL);
                theCallIntent.setData(Uri.parse("tel:" + theUser.getContactNumber())); //parse the telephone number as URI
                theContext.startActivity(theCallIntent); //open the call intent
                return true;
            } else if (item.getItemId() == R.id.delete_click) {
                //user click delete
                //construct confirmation dialog
                AlertDialog theDeleteDialog = new MaterialAlertDialogBuilder(theContext)
                        .setTitle("Would you like to delete this user?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            //user click delete.
                            deleteUserInDb(theUser);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            //user clicks cancel
                            dialog.cancel();
                        })
                        .show();
                return true;
            } else if (item.getItemId() == R.id.email_click) {
                //user clicks email
                //construct a app chooser that is capable of sending emails.
                Intent theEmailIntent = new Intent(Intent.ACTION_SENDTO);
                theEmailIntent.setType("text/plain");
                theEmailIntent.putExtra(Intent.EXTRA_EMAIL, theUser.getEmailAddress());
                theContext.startActivity(Intent.createChooser(theEmailIntent, "Contact User"));
                return true;
            } else {
                return false;
            }
        });

        holder.getMore().setOnClickListener(v -> {
            //user click show more, open the popup menu
            theMoreOption.show();
        });
    }

    private void deleteUserInDb(User theUser) {
        //delete the user by making the api call.
        HashMap<String, String> deleteBody = new HashMap<>();
        deleteBody.put("username", theUser.getUsername());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView more;
        private final MaterialTextView username;
        private final MaterialTextView fullName;
        private final MaterialTextView emailAddress;
        private final MaterialTextView contactNumber;
        private final MaterialTextView dateOfBirth;
        private final MaterialTextView memberSince;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            more = itemView.findViewById(R.id.more_options);
            fullName = itemView.findViewById(R.id.full_name);
            emailAddress = itemView.findViewById(R.id.email_address);
            contactNumber = itemView.findViewById(R.id.contact_number);
            dateOfBirth = itemView.findViewById(R.id.date_of_birth);
            memberSince = itemView.findViewById(R.id.member_since);
        }

        public ImageView getMore() {
            return more;
        }

        public MaterialTextView getUsername() {
            return username;
        }

        public MaterialTextView getFullName() {
            return fullName;
        }

        public MaterialTextView getEmailAddress() {
            return emailAddress;
        }

        public MaterialTextView getContactNumber() {
            return contactNumber;
        }

        public MaterialTextView getDateOfBirth() {
            return dateOfBirth;
        }

        public MaterialTextView getMemberSince() {
            return memberSince;
        }
    }
}
