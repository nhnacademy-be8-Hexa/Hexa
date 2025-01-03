package com.nhnacademy.hello.dto.dooray;

import java.util.List;

public class MessagePayloadDTO {
    private String botName;
    private String text;
    private List<Attachment> attachments;

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public static class Attachment {
        private String title;
        private String text;
        private String titleLink;
        private String botIconImage;
        private String color;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getTitleLink() {
            return titleLink;
        }

        public void setTitleLink(String titleLink) {
            this.titleLink = titleLink;
        }

        public String getBotIconImage() {
            return botIconImage;
        }

        public void setBotIconImage(String botIconImage) {
            this.botIconImage = botIconImage;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }
}
