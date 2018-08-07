/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.eulerframework.web.module.authentication.entity;

import java.util.Date;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import net.eulerframework.web.core.base.entity.NonIDEntity;
import net.eulerframework.web.module.authentication.enums.Gender;
import net.eulerframework.web.module.authentication.enums.NameOrder;

/**
 * @author cFrost
 *
 */
@Entity
@Table(name = "sys_basic_user_profile")
public class BasicUserProfile extends NonIDEntity<BasicUserProfile, String> implements EulerUserProfileEntity {
    
    /**
     * 用户ID
     */
    @Id
    @Column(name = "user_id", length = 36)
    private String userId;
    /**
     * 名字
     */
    @Column(name = "given_name", length = 255, nullable = true)
    private String givenName;
    /**
     * 姓氏
     */
    @Column(name = "fmaily_name", length = 255, nullable = true)
    private String familyName;
    /**
     * 性别
     */
    @Column(name = "gender", length = 6, nullable = true)
    @Enumerated(EnumType.STRING)
    private Gender gender;
    /**
     * 国籍
     */
    @Column(name = "nationality", length = 255, nullable = true)
    private String nationality;
    /**
     * 州/省
     */
    @Column(name = "province", length = 255, nullable = true)
    private String province;
    /**
     * 民族
     */
    @Column(name = "ethnicity", length = 255, nullable = true)
    private String ethnicity;
    /**
     * 国家/地区
     */
    @Column(name = "country_or_region", length = 255, nullable = true)
    private String countryOrRegion;
    /**
     * 城市
     */
    @Column(name = "city", length = 255, nullable = true)
    private String city;
    /**
     * 地址
     */
    @Column(name = "address", length = 2000, nullable = true)
    private String address;
    /**
     * 出生日期
     */
    @Column(name = "date_of_birth", nullable = true)
    private Date dateOfBirth;
    /**
     * 偏好语言
     */
    @Column(name = "preferred_language", length = 20, nullable = true)
    private Locale preferredLanguage;
    /**
     * 姓名显示顺序
     */
    @Column(name = "name_order", length = 17, nullable = true)
    @Enumerated(EnumType.STRING)
    private NameOrder nameOrder;

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getCountryOrRegion() {
        return countryOrRegion;
    }

    public void setCountryOrRegion(String countryOrRegion) {
        this.countryOrRegion = countryOrRegion;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Locale getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(Locale preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public NameOrder getNameOrder() {
        return nameOrder;
    }

    public void setNameOrder(NameOrder nameOrder) {
        this.nameOrder = nameOrder;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getUserId() {
        return this.userId;
    }

    @Override
    public String getId() {
        return this.userId;
    }

    @Override
    public void setId(String id) {
        this.userId = id;
    }

    @Override
    public int compareTo(BasicUserProfile o) {
        return this.getId().compareTo(o.getId());
    }

}
