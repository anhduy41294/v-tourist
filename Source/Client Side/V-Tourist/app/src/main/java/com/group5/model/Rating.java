package com.group5.model;

/**
 * Rating of Place
 */
public class Rating {
    /**
     * Id
     */
    private String mId;
    /**
     * PlaceId
     */

    private String mPlaceId;
    /**
     * User Rate
     */
    private User mUserRate;
    /**
     * Score
     */
    private float mScore;
    /**
     * Comment
     */
    private String mComment;

    /**
     * Get/Set properties
     */

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getPlaceId() {
        return mPlaceId;
    }

    public void setPlaceId(String placeId) {
        this.mPlaceId = placeId;
    }

    public User getUserRate() {
        return mUserRate;
    }

    public void setUserRate(User mUserRate) {
        this.mUserRate = mUserRate;
    }

    public float getScore() {
        return mScore;
    }

    public void setScore(float mScore) {
        this.mScore = mScore;
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String mComment) {
        this.mComment = mComment;
    }

    /**
     * Constructor
     * @param mUserRate
     * @param placeId
     * @param mScore
     * @param mComment
     */
    public Rating(User mUserRate, String placeId, float mScore, String mComment) {
        this.mPlaceId = placeId;
        this.mUserRate = mUserRate;
        this.mScore = mScore;
        this.mComment = mComment;
    }

    /**
     * Constructor
     */
    public Rating() {
    }
}
