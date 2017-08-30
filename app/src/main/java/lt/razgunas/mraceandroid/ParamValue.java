package lt.razgunas.mraceandroid;

import android.support.annotation.NonNull;

import java.util.List;

public class ParamValue {
    private String paramName;
    private int paramIndex;
    private float paramValue;
    private Short paramType;

    ParamValue(String name, int index, float value, Short type) {
        this.paramName = name;
        this.paramIndex = index;
        this.paramValue = value;
        this.paramType = type;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public int getParamIndex() {
        return paramIndex;
    }

    public void setParamIndex(int paramIndex) {
        this.paramIndex = paramIndex;
    }

    public float getParamValue() {
        return paramValue;
    }

    public void setParamValue(float paramValue) {
        this.paramValue = paramValue;
    }

    public Short getParamType() {
        return paramType;
    }

    public void setParamType(Short paramType) {
        this.paramType = paramType;
    }
}
