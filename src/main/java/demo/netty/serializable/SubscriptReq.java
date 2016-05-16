package demo.netty.serializable;

import java.io.Serializable;

/**
 * Created by Y on 2016/5/16.
 * 订购请求
 * subReqId 订单编号
 * userName 用户名
 * produceName 产品名称
 * phoneNumber 订购者电话号码
 * address 订购者的家庭住址
 */
public class SubscriptReq implements Serializable {

    private static final long serialVersionUID = 1L;

    private int subReqID;
    private String userName;
    private String productName;
    private String phoneNumber;
    private String address;

    public int getSubReqID() {
        return subReqID;
    }

    public void setSubReqID(int subReqID) {
        this.subReqID = subReqID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "SubscriptReq{" +
                "subReqID=" + subReqID +
                ", userName='" + userName + '\'' +
                ", productName='" + productName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
