package com.universalvideoviewsample;

import java.util.List;

public class PlayListResp {
    private String Message;
    private int RetCode;
    private boolean Success;
    private List<DataBean> Data;

    public String getMessage() { return Message;}

    public void setMessage(String Message) { this.Message = Message;}

    public int getRetCode() { return RetCode;}

    public void setRetCode(int RetCode) { this.RetCode = RetCode;}

    public boolean isSuccess() { return Success;}

    public void setSuccess(boolean Success) { this.Success = Success;}

    public List<DataBean> getData() { return Data;}

    public void setData(List<DataBean> Data) { this.Data = Data;}

    public static class DataBean {
        private String __type;
        private String LatelyUpdateTime;
        private String Name;
        private String Uri;

        public String get__type() { return __type;}

        public void set__type(String __type) { this.__type = __type;}

        public String getLatelyUpdateTime() { return LatelyUpdateTime;}

        public void setLatelyUpdateTime(String LatelyUpdateTime) { this.LatelyUpdateTime = LatelyUpdateTime;}

        public String getName() { return Name;}

        public void setName(String Name) { this.Name = Name;}

        public String getUri() { return Uri;}

        public void setUri(String Uri) { this.Uri = Uri;}
    }
}
