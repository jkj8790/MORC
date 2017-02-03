package morc.helpme.kr.morc.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import morc.helpme.kr.morc.R;
import morc.helpme.kr.morc.model.LogInfo;

public class LogFragment extends Fragment {

  @BindView(R.id.recyclerview) RecyclerView recyclerView;
  private LogAdapter logAdapter;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_log, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);

    setupUI();
  }

  private void setupUI() {
    // use this setting to improve performance if you know that changes
    // in content do not change the layout size of the RecyclerView
    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    logAdapter = new LogAdapter(recyclerView);
    recyclerView.setAdapter(logAdapter);

    logAdapter.addLogInfo(new LogInfo("title", "date", "type", "exception"));
  }
}