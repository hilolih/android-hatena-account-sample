package com.example.hatenaaccountsample;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class Setting extends PreferenceActivity {
    public static final String MODE = "mode";

    public static final String ACCOUNT          = "account";
    public static final String ACCOUNT_NAME     = "account_name";
    public static final String ACCOUNT_PASSWORD = "account_password";
    public static final String ACCOUNT_RK       = "account_rk";
    public static final String ACCOUNT_RKM      = "account_rkm";
    
    protected static final int USER_REQUEST     = 1;
    protected SharedPreferences mPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mPref = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = getIntent();
        String mode = intent.getStringExtra(MODE);

        // 他の Activity から
        //   startActivity(new Intent(this, Setting.class).putExtra(Setting.MODE, Setting.ACCOUNT));
        // で起動した場合、再認証する
        if (mode != null && mode.equals(ACCOUNT)) {
            final String username  = mPref.getString(ACCOUNT_NAME, null);
            userRequest(username);
        }

        updateAccountSummary();

        PreferenceScreen pAccount = (PreferenceScreen) findPreference(ACCOUNT);
        pAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                String username = getPreferenceManager().getSharedPreferences().getString(ACCOUNT_NAME, null);
                if (username != null) {
                    // 既にログインしている場合はログアウトするダイアログ
                    new AlertDialog.Builder(Setting.this)
                        .setIcon(R.drawable.icon)
                        .setTitle(R.string.already_logged_in)
                        .setMessage(getString(R.string.already_logged_in_message, username))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SharedPreferences.Editor editor = mPref.edit();
                                editor.remove(ACCOUNT_NAME);
                                editor.remove(ACCOUNT_RK);
                                editor.remove(ACCOUNT_RKM);
                                editor.commit();
                                updateAccountSummary();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();
                } else {
                    // ログインしてないならログインリクエスト
                    userRequest(null);
                }
                return true;
            }
        });
    }

    private void updateAccountSummary () {
        // 現在の状態をユーザに表示
        PreferenceScreen pAccount = (PreferenceScreen) findPreference(ACCOUNT);
        String username = mPref.getString(ACCOUNT_NAME, null);
        if (username != null) {
            pAccount.setTitle(getString(R.string.account_title_logout));
            pAccount.setSummary(getString(R.string.account_summary_loggedin, username));
        } else {
            pAccount.setTitle(getString(R.string.account_title));
            pAccount.setSummary(getString(R.string.account_summary));
        }
    }

    protected void userRequest(String username) {
        // 必要に応じてアカウント管理へユーザ情報をリクエスト
        // アカウント管理がインストールされてない場合はマーケットへ
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("vnd.android.cursor.dir/vnd.hatena.accounts");
        if (username != null) intent.putExtra("account", username);
        try {
            startActivityForResult(intent, USER_REQUEST);
        } catch (ActivityNotFoundException e) {
            new AlertDialog.Builder(this)
                .setTitle(R.string.install_hatena_account_title)
                .setMessage(R.string.install_hatena_account_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Uri uri = Uri.parse("market://search?q=pname:com.hatena.android.accounts");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                })
                .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == USER_REQUEST) {
            if (resultCode == RESULT_OK) {
                // 取得した情報を保存しておく
                String username  = data.getStringExtra("username");
                String randomkey = data.getStringExtra("randomkey");
                String rkm       = data.getStringExtra("rkm");

                SharedPreferences.Editor editor = mPref.edit();
                editor.putString(ACCOUNT_NAME, username);
                editor.putString(ACCOUNT_RK, randomkey);
                editor.putString(ACCOUNT_RKM, rkm);
                editor.commit();

                updateAccountSummary();
            }
        }
    }
}

