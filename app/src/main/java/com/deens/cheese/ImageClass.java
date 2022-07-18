package com.deens.cheese;

public class ImageClass {
    String imageURL;
    String imageTime;
    String imageName;

    public ImageClass(String imageURL, String imageTime, String imageName) {
        this.imageURL = imageURL;
        this.imageTime = imageTime;
        this.imageName = imageName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getImageTime() {
        return imageTime;
    }

    public void setImageTime(String imageTime) {
        this.imageTime = imageTime;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
