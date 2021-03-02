package ru.didim99.tstu.core.knowledge.rulebase;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by didim99 on 02.03.21.
 */

public class Variable {

  public enum Type {
    @SerializedName("boolean")
    BOOLEAN,
    @SerializedName("float")
    FLOAT,
    @SerializedName("enum")
    ENUM
  }

  @SerializedName("name")
  private String name;
  @SerializedName("type")
  private Type type;

  private String value;
  private List<String> values;

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }
}
