package app.m4ntis.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import app.m4ntis.sample.databinding.MainActivityBinding;

public class MainActivity extends FragmentActivity {

  MainActivityBinding binding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil
        .setContentView(this, R.layout.main_activity);
  }
}
