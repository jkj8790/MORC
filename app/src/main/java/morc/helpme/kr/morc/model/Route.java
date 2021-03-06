package morc.helpme.kr.morc.model;

import android.text.TextUtils;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import java.util.regex.Pattern;

public class Route extends RealmObject {
  @PrimaryKey public int id;
  public String title;
  public String from;
  public String regex;
  public String authorization;
  public String tag;
  public RealmList<RealmString> urlList;
  public boolean enabled;

  public void initialize(String title, String from, String regex,
      String authorization, String tag, RealmList<RealmString> urlList, boolean enabled) {
    this.title = title;
    this.from = from;
    this.regex = regex;
    this.authorization = authorization;
    this.urlList = urlList;
    this.tag = tag;
    this.enabled = enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean satisfyCondition(String messageFrom, String msg) {
    if(TextUtils.isEmpty(from) && TextUtils.isEmpty(regex))
      return false;
    else if(!TextUtils.isEmpty(from) && !TextUtils.isEmpty(regex)) {
      Pattern p = Pattern.compile(regex);
      return from.equals(messageFrom) && p.matcher(msg).matches();
    } else if(!TextUtils.isEmpty(regex)) {
      Pattern p = Pattern.compile(regex);
      return p.matcher(msg).matches();
    } else {
      return from.equals(messageFrom);
    }
  }

}
