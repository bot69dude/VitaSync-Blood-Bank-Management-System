package com.vitasync.dto;

import com.vitasync.entity.User;
import com.vitasync.enums.BloodType;
import com.vitasync.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String city;
    private String address;
    private BloodType bloodType;
    private UserRole role;
    private Double longitude;
    private Double latitude;
    private Boolean isAvailable;
    private java.time.LocalDate lastDonationDate;
    private java.time.LocalDate createdAt;
    private java.time.LocalDate updatedAt;
    private Boolean autoScheduleEnabled;
    private Integer transfusionFrequencyDays;
    private java.time.LocalDate nextTransfusionDate;
    private String preferredHospitalName;
    private String preferredHospitalAddress;

    // Construct from entity, intentionally excluding password
    public UserResponse(User u) {
        if (u == null) return;
        this.id = u.getId();
        this.name = u.getName();
        this.email = u.getEmail();
        this.phone = u.getPhone();
        this.city = u.getCity();
        this.address = u.getAddress();
        this.bloodType = u.getBloodType();
        this.role = u.getRole();
        this.longitude = u.getLongitude();
        this.latitude = u.getLatitude();
        this.isAvailable = u.getIsAvailable();
        this.lastDonationDate = u.getLastDonationDate();
        this.createdAt = u.getCreatedAt();
        this.updatedAt = u.getUpdatedAt();
        this.autoScheduleEnabled = u.getAutoScheduleEnabled();
        this.transfusionFrequencyDays = u.getTransfusionFrequencyDays();
        this.nextTransfusionDate = u.getNextTransfusionDate();
        this.preferredHospitalName = u.getPreferredHospitalName();
        this.preferredHospitalAddress = u.getPreferredHospitalAddress();
    }
}
