package com.world.myapplication;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;
import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;

public class SearchFragmentDirections {
  private SearchFragmentDirections() {
  }

  @NonNull
  public static SearchToResult searchToResult() {
    return new SearchToResult();
  }

  public static class SearchToResult implements NavDirections {
    private final HashMap arguments = new HashMap();

    private SearchToResult() {
    }

    @NonNull
    public SearchToResult setArtist(@NonNull String artist) {
      if (artist == null) {
        throw new IllegalArgumentException("Argument \"artist\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("artist", artist);
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    public Bundle getArguments() {
      Bundle __result = new Bundle();
      if (arguments.containsKey("artist")) {
        String artist = (String) arguments.get("artist");
        __result.putString("artist", artist);
      } else {
        __result.putString("artist", "1");
      }
      return __result;
    }

    @Override
    public int getActionId() {
      return R.id.search_to_result;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public String getArtist() {
      return (String) arguments.get("artist");
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) {
          return true;
      }
      if (object == null || getClass() != object.getClass()) {
          return false;
      }
      SearchToResult that = (SearchToResult) object;
      if (arguments.containsKey("artist") != that.arguments.containsKey("artist")) {
        return false;
      }
      if (getArtist() != null ? !getArtist().equals(that.getArtist()) : that.getArtist() != null) {
        return false;
      }
      if (getActionId() != that.getActionId()) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + (getArtist() != null ? getArtist().hashCode() : 0);
      result = 31 * result + getActionId();
      return result;
    }

    @Override
    public String toString() {
      return "SearchToResult(actionId=" + getActionId() + "){"
          + "artist=" + getArtist()
          + "}";
    }
  }
}
