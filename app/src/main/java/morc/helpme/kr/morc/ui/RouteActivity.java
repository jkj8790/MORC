package morc.helpme.kr.morc.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java.util.ArrayList;
import java.util.List;
import morc.helpme.kr.morc.Log;
import morc.helpme.kr.morc.R;
import morc.helpme.kr.morc.model.RouteInfo;

public class RouteActivity extends AppCompatActivity {

  private static final int TYPE_NEW = 0;
  private static final int TYPE_EDIT = 2;

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.layout_urls) LinearLayout layout;
  @BindView(R.id.edit_title) TextInputEditText editTitle;
  @BindView(R.id.edit_from) TextInputEditText editFrom;
  @BindView(R.id.edit_regex) TextInputEditText editRegex;
  private int type;

  private List<TextInputEditText> urlViewList;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_route);
    ButterKnife.bind(this);
    setSupportActionBar(toolbar);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowHomeEnabled(true);

    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        finish();
      }
    });

    urlViewList = new ArrayList<>();

    if(getIntent().getParcelableExtra("Route") == null) {
      type = TYPE_NEW;
      addUrlView();
      getSupportActionBar().setTitle("Add New Routing Logic");
    } else {
      type = TYPE_EDIT;
      readRouteInfo((RouteInfo) getIntent().getParcelableExtra("Route"));
    }
  }

  @OnClick(R.id.button_add_url) void onClickButton() {
    addUrlView();
  }

  private void addUrlView() {
    addUrlView(null);
  }

  private void addUrlView(String text) {
    LayoutInflater inflater = (LayoutInflater) getSystemService (Context.LAYOUT_INFLATER_SERVICE);
    View view = inflater.inflate(R.layout.layout_url, null);
    final TextInputEditText textInputEditText = (TextInputEditText) view.findViewById(R.id.edit_url);
    textInputEditText.setText(text);
    textInputEditText.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(!Patterns.WEB_URL.matcher(charSequence).matches()) {
          textInputEditText.setError("유효한 url이 아닙니다");
        }
      }

      @Override public void afterTextChanged(Editable editable) {

      }
    });
    urlViewList.add(textInputEditText);
    layout.addView(view);
  }

  private void readRouteInfo(RouteInfo routeInfo) {
    editTitle.setText(routeInfo.title);
    editFrom.setText(routeInfo.from);
    editRegex.setText(routeInfo.regex);
    for(int i = 0; i < routeInfo.urlList.size(); i++) {
      addUrlView(routeInfo.urlList.get(i));
    }
    getSupportActionBar().setTitle(routeInfo.title);
  }

  private List<String> getUrlsFromViewList() {
    List<String> urlList = new ArrayList<>(urlViewList.size());
    for(int i = 0; i < urlViewList.size(); i++) {
      urlList.add(urlViewList.get(i).getText().toString());
    }
    return urlList;
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    switch (type) {
      case TYPE_NEW:
        getMenuInflater().inflate(R.menu.menu_route, menu);
        break;
      case TYPE_EDIT:
        getMenuInflater().inflate(R.menu.menu_route_edit, menu);
        break;
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_save:
        //TODO 예외처리
        RouteInfo routeInfo = new RouteInfo(editTitle.getText().toString(),
            editFrom.getText().toString(), editRegex.getText().toString(), getUrlsFromViewList(), true);

        Intent intent = new Intent();
        intent.putExtra("Route", routeInfo);
        setResult(RESULT_OK, intent);
        finish();
        return true;
      case R.id.action_delete:
        new AlertDialog.Builder(this)
            .setTitle("삭제")
            .setMessage("정말 삭제하시겠습니까?")
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
              @Override public void onClick(DialogInterface dialogInterface, int i) {
                Intent removeIntent = new Intent();
                removeIntent.putExtra("remove", 1);
                setResult(RESULT_OK, removeIntent);
                finish();
              }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
