package ru.didim99.tstu.core.knowledge.rulebase;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by didim99 on 02.03.21.
 */

public class Variable {
  public static final String BOOLEAN_TRUE = "true";
  public static final String BOOLEAN_FALSE = "false";

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
  private boolean userDefined;

  public Variable() {
    this.userDefined = false;
  }

  public Variable(Variable src) {
    this.userDefined = false;
    this.name = src.name;
    this.type = src.type;
    this.value = src.value;

    if (src.values != null) {
      this.values = new ArrayList<>();
      this.values.addAll(src.values);
    }
  }

  public Variable(ValueHolder source) {
    this.userDefined = false;
    this.name = source.getVariable();
    this.value = source.getValue();
  }

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

  public String[] getValuesArray() {
    return values.toArray(new String[0]);
  }

  public boolean isUserDefined() {
    return userDefined;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public void setUserDefined(boolean userDefined) {
    this.userDefined = userDefined;
  }

  public void setValueByIndex(int index) {
    if (type != Type.ENUM)
      throw new IllegalStateException("Unsupported operation for type: " + type);
    this.value = values.get(index);
  }

  public boolean equals(RuleTarget target) {
    return target.getVariable().equals(name)
      && target.getValue().equals(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Variable variable = (Variable) o;
    return name.equals(variable.name);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(new Object[] {name, type});
  }

  @Override
  public String toString() {
    return "Variable{" +
      "name='" + name + '\'' +
      ", value='" + value + '\'' +
      '}';
  }
}
