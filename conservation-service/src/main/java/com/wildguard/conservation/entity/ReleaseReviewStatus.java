package com.wildguard.conservation.entity;

// Before an animal is released the rescue officer has to REQUEST a review
// and an officer has to APPROVE or REJECT it. NONE means nobody asked yet.
public enum ReleaseReviewStatus {
    NONE, REQUESTED, APPROVED, REJECTED
}
