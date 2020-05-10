package com.world.myapplication;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

public class FirstPageDirections {
  private FirstPageDirections() {
  }

  @NonNull
  public static NavDirections actionFirstPageToSearchFragment() {
    return new ActionOnlyNavDirections(R.id.action_first_page_to_search_fragment);
  }
}
