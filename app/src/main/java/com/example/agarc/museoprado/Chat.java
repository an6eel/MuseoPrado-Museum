package com.example.agarc.museoprado;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.github.bassaer.chatmessageview.model.IChatUser;
import com.github.bassaer.chatmessageview.model.Message;
import com.github.bassaer.chatmessageview.view.MessageView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;


public class Chat {

    MessageView chat;
    final IChatUser you;
    final IChatUser agent;

    public Chat(MessageView ch){
        chat = ch;
        you = new IChatUser() {
            @NotNull
            @Override
            public String getId() {
                return "0";
            }

            @Nullable
            @Override
            public String getName() {
                return "You";
            }

            @Nullable
            @Override
            public Bitmap getIcon() {
                return null;
            }

            @Override
            public void setIcon(@NotNull Bitmap bitmap) {

            }
        };
        agent = new IChatUser() {
            @NotNull
            @Override
            public String getId() {
                return "1";
            }

            @Nullable
            @Override
            public String getName() {
                return "Diego Velazquez";
            }

            @Nullable
            @Override
            public Bitmap getIcon() {
                return null;
            }

            @Override
            public void setIcon(@NotNull Bitmap bitmap) {

            }
        };

        this.conf();
    }

    private void conf(){
        chat.init();
        chat.setRightBubbleColor(Color.parseColor("#5d3e2b"));
        chat.setLeftBubbleColor(Color.WHITE);
        chat.setRightMessageTextColor(Color.WHITE);
        chat.setLeftMessageTextColor(Color.BLACK);
        chat.setUsernameTextColor(Color.BLACK);
        chat.setSendTimeTextColor(Color.BLACK);
        chat.setDateSeparatorTextColor(Color.BLACK);
        chat.setMessageMarginTop(5);
        chat.setMessageMarginBottom(15);
    }

    protected void putRequest(String msg){
        Message msg1 = new Message.Builder()
                .setUser(you)
                .hideIcon(true)
                .setDateCell(true)
                .setRight(false)
                .setText(msg).build();

        chat.setMessage(msg1);
        chat.scrollToEnd();
    }

    protected void putResponse(String msg, Message.Type type,Bitmap image){
        Message msg1 = new Message.Builder()
                .setUser(agent)
                .hideIcon(true)
                .setDateCell(true)
                .setRight(true).build();
        if(type!=null)
            msg1.setType(type);

        switch (type){
            case PICTURE:
                msg1.setPicture(image);
                break;
            default:
                msg1.setText(msg);
                break;
        }

        chat.setMessage(msg1);
        chat.scrollToEnd();
    }

    protected ArrayList<Message> getLog(){
        return chat.getMessageList();
    }
}
