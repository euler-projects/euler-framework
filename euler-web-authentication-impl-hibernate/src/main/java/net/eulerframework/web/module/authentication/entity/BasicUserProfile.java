/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2017 cFrost.sun(孙宾, SUN BIN) 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * For more information, please visit the following website
 * 
 * https://eulerproject.io
 * https://github.com/euler-form/web-form
 * https://cfrost.net
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
@Table(name = "SYS_BASIC_USER_PROFILE")
public class BasicUserProfile extends NonIDEntity<BasicUserProfile, String> implements EulerUserProfileEntity {
    
    /**
     * 用户ID
     */
    @Id
    @Column(name = "USER_ID", length = 36)
    private String userId;
    /**
     * 名字
     */
    @Column(name = "GIVEN_NAME", length = 255, nullable = true)
    private String givenName;
    /**
     * 姓氏
     */
    @Column(name = "FAMILY_NAME", length = 255, nullable = true)
    private String familyName;
    /**
     * 性别
     */
    @Column(name = "GENDER", length = 6, nullable = true)
    @Enumerated(EnumType.STRING)
    private Gender gender;
    /**
     * 国籍
     */
    @Column(name = "NATIONALITY", length = 255, nullable = true)
    private String nationality;
    /**
     * 州/省
     */
    @Column(name = "PROVINCE", length = 255, nullable = true)
    private String province;
    /**
     * 民族
     */
    @Column(name = "ETHNICITY", length = 255, nullable = true)
    private String ethnicity;
    /**
     * 国家/地区
     */
    @Column(name = "COUNTRY_OR_REGION", length = 255, nullable = true)
    private String countryOrRegion;
    /**
     * 城市
     */
    @Column(name = "CITY", length = 255, nullable = true)
    private String city;
    /**
     * 地址
     */
    @Column(name = "ADDRESS", length = 2000, nullable = true)
    private String address;
    /**
     * 出生日期
     */
    @Column(name = "DATE_OF_BIRTH", nullable = true)
    private Date dateOfBirth;
    /**
     * 偏好语言
     */
    @Column(name = "PREFERRED_LANG", length = 20, nullable = true)
    private Locale preferredLanguage;
    /**
     * 姓名显示顺序
     */
    @Column(name = "NAME_ORDER", length = 17, nullable = true)
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
