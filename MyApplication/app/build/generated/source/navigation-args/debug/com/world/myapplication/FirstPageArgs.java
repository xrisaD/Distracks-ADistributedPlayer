package com.world.myapplication;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.navigation.NavArgs;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;

public class FirstPageArgs implements NavArgs {
  private final HashMap arguments = new HashMap();

  private FirstPageArgs() {
  }

  private FirstPageArgs(HashMap argumentsMap) {
    this.arguments.putAll(argumentsMap);
  }

  @NonNull
  @SuppressWarnings("unchecked")
  public static FirstPageArgs fromBundle(@NonNull Bundle bundle) {
    FirstPageArgs __result = new FirstPageArgs();
    bundle.setClassLoader(FirstPageArgs.class.getClassLoader());
    if (bundle.containsKey("myArg")) {
      int myArg;
      myArg = bundle.getInt("myArg");
      __result.arguments.put("myArg", myArg);
    } else {
      __result.arguments.put("myArg", 1);
    }
    return __result;
  }

  @SuppressWarnings("unchecked")
  public int getMyArg() {
    return (int) arguments.get("myArg");
  }

  @SuppressWarnings("unchecked")
  @NonNull
  public Bundle toBundle() {
    Bundle __result = new Bundle();
    if (arguments.containsKey("myArg")) {
      int myArg = (int) arguments.get("myArg");
      __result.putInt("myArg", myArg);
    } else {
      __result.putInt("myArg", 1);
    }
    return __result;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
        return true;
    }
    if (object == null || getClass() != object.getClass()) {
        return false;
    }
    FirstPageArgs that = (FirstPageArgs) object;
    if (arguments.containsKey("myArg") != that.arguments.containsKey("myArg")) {
      return false;
    }
    if (getMyArg() != that.getMyArg()) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + getMyArg();
    return result;
  }

  @Override
  public String toString() {
    return "FirstPageArgs{"
        + "myArg=" + getMyArg()
        + "}";
  }

  public static class Builder {
    private final HashMap arguments = new HashMap();

    public Builder(FirstPageArgs original) {
      this.arguments.putAll(original.arguments);
    }

    public Builder() {
    }

    @NonNull
    public FirstPageArgs build() {
      FirstPageArgs result = new FirstPageArgs(arguments);
      return result;
    }

    @NonNull
    public Builder setMyArg(int myArg) {
      this.arguments.put("myArg", myArg);
      return this;
    }

    @SuppressWarnings("unchecked")
    public int getMyArg() {
      return (int) arguments.get("myArg");
    }
  }
}
