package me.ghini.pocket;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.net.MalformedURLException;
import java.net.URL;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class DesktopClientActivity extends AppCompatActivity {

    static final String SERVER_IP_ADDRESS = "ServerIPAddress";
    static final String SERVER_PORT = "ServerPort";
    static final String USER_NAME = "UserName";

    private Bundle state;
    private String securityCode = "";
    private String urlText;
    private String deviceId = "";
    private EditText etServerIPAddress;
    private EditText etServerPort;
    private EditText etSecurityCode;
    private EditText etUserName;

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putAll(this.state);
        super.onSaveInstanceState(state);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.state = new Bundle();
        if (savedInstanceState != null) {
            state.putAll(savedInstanceState);
        } else {
            state.putString(SERVER_IP_ADDRESS, "192.168.43.226");
            state.putString(SERVER_PORT, "44464");
            state.putString(USER_NAME, "");
        }
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        try {
            if (telephonyManager != null) deviceId = telephonyManager.getDeviceId();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.permit_imei, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
        setContentView(R.layout.activity_desktop_client);
        etServerIPAddress = (EditText) findViewById(R.id.etServerIPAddress);
        etServerIPAddress.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable target) {
                    afterServerAddressChanged(target);
                }
        });
        etServerPort = (EditText) findViewById(R.id.etServerPort);
        etServerPort.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable target) {
                    afterServerPortChanged(target);
                }
        });
        etUserName = (EditText) findViewById(R.id.etUserName);
        etUserName.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable target) {
                    afterUserNameChanged(target);
                }
        });
        etSecurityCode = (EditText) findViewById(R.id.etSecurityCode);
        etSecurityCode.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable target) {
                    afterSecurityCodeChanged(target);
                }
        });
        recomputeUrlText();
    }

    private void afterSecurityCodeChanged(Editable target) {
        securityCode = String.valueOf(etSecurityCode.getText());
    }

    private void afterServerPortChanged(Editable target) {
        state.putString("ServerPort", String.valueOf(etServerPort.getText()));
        recomputeUrlText();
    }

    private void afterUserNameChanged(Editable target) {
        state.putString("UserName", String.valueOf(etUserName.getText()));
    }

    private void afterServerAddressChanged(Editable target) {
        state.putString("ServerIPAddress", String.valueOf(etServerIPAddress.getText()));
        recomputeUrlText();
    }

    private void recomputeUrlText() {
        urlText = "http://"+ state.getString(SERVER_IP_ADDRESS) +":"+state.get(SERVER_PORT)+"/API1";
    }

    public void onVerify(final View view) {
        final Activity activity = this;
        try {
            XMLRPCCallback listener = new XMLRPCCallback() {
                public void onResponse(long id, Object result) {
                    final Object o = result;
                    if((result instanceof Integer) && ((Integer) result).intValue() != 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        final String userName = result.toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((EditText) findViewById(R.id.etUserName)).setText(userName);
                            }
                        });
                    }
                }
                public void onError(long id, XMLRPCException error) {
                    final Object o = error;
                    // Handling any error in the library
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                public void onServerError(long id, XMLRPCServerException error) {
                    final Object o = error;
                    // Handling an error response from the server
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };
            XMLRPCClient client = new XMLRPCClient(new URL(urlText));
            client.callAsync(listener, "verify", deviceId);
        } catch (MalformedURLException e) {
            Toast.makeText(this, "Malformed URL", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            Toast.makeText(this, "Some Error "+e, Toast.LENGTH_SHORT).show();
        }
    }

    public void onRegister(View view) {
        final Activity activity = this;
        try {
            XMLRPCCallback listener = new XMLRPCCallback() {
                public void onResponse(long id, Object result) {
                    final Object o = result;
                    runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                }
                public void onError(long id, XMLRPCException error) {
                    final Object o = error;
                    // Handling any error in the library
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                public void onServerError(long id, XMLRPCServerException error) {
                    final Object o = error;
                    // Handling an error response from the server
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };
            XMLRPCClient client = new XMLRPCClient(new URL(urlText));
            client.callAsync(listener, "register", deviceId, state.getString(USER_NAME), securityCode);
        } catch (MalformedURLException e) {
            Toast.makeText(this, "Malformed URL", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            Toast.makeText(this, "Some Error "+e, Toast.LENGTH_SHORT).show();
        }
    }

    public void onPush(View view) {
    }

    public void onPull(View view) {
    }
}

