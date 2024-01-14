package com.agifans.agile.config;

import java.util.ArrayList;

public class AppConfig {

  private ArrayList<AppConfigItem> apps;

  public AppConfig() {
    this.apps = new ArrayList<AppConfigItem>();
  }
  
  /**
   * @return the apps
   */
  public ArrayList<AppConfigItem> getApps() {
    return apps;
  }

  /**
   * @param apps the apps to set
   */
  public void setApps(ArrayList<AppConfigItem> apps) {
    this.apps = apps;
  }
}
