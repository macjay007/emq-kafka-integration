package com.mac.log.entity;

/**
*
* @author zj
* @Date 2024/7/23 13:11 
**/
public class BizLogInfo {
    private String bizLogContent;

    public BizLogInfo(String bizLogContent) {
        this.bizLogContent = bizLogContent;
    }

    public String getBizLogContent() {
        return this.bizLogContent;
    }

    public void setBizLogContent(final String bizLogContent) {
        this.bizLogContent = bizLogContent;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof BizLogInfo)) {
            return false;
        } else {
            BizLogInfo other = (BizLogInfo)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$bizLogContent = this.getBizLogContent();
                Object other$bizLogContent = other.getBizLogContent();
                if (this$bizLogContent == null) {
                    if (other$bizLogContent != null) {
                        return false;
                    }
                } else if (!this$bizLogContent.equals(other$bizLogContent)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BizLogInfo;
    }

    public int hashCode() {
        boolean PRIME = true;
        int result = 1;
        Object $bizLogContent = this.getBizLogContent();
        result = result * 59 + ($bizLogContent == null ? 43 : $bizLogContent.hashCode());
        return result;
    }

    public String toString() {
        return "BizLogInfo(bizLogContent=" + this.getBizLogContent() + ")";
    }

    public BizLogInfo() {
    }
}
