package com.example.fragmentos;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class NewFragments extends Fragment {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorEventListener;
    private TextView xValueTextView, yValueTextView, zValueTextView;

    private long lastUpdateTime = 0;
    private float lastZ;
    private static final int SHAKE_THRESHOLD = 800;
    private static final int DOUBLE_TAP_TIME_DELTA = 400; // Milisegundos
    private long lastTapTimeMs = 0;
    private int tapCount = 0;

    public NewFragments() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_fragments, container, false);

        // Inicializa los TextViews
        xValueTextView = view.findViewById(R.id.xValueTextView);
        yValueTextView = view.findViewById(R.id.yValueTextView);
        zValueTextView = view.findViewById(R.id.zValueTextView);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                long currentTime = System.currentTimeMillis();

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                // Actualiza los TextViews con los valores actuales
                xValueTextView.setText("X: " + x);
                yValueTextView.setText("Y: " + y);
                zValueTextView.setText("Z: " + z);

                if (lastUpdateTime == 0) {
                    lastUpdateTime = currentTime;
                    lastZ = z;
                    return;
                }

                long timeDifference = currentTime - lastUpdateTime;
                if (timeDifference > 100) { // Intervalo mínimo entre lecturas para evitar el ruido.
                    float speed = Math.abs(z - lastZ) / timeDifference * 10000;

                    if (speed > SHAKE_THRESHOLD) {
                        long tapTimeMs = currentTime;
                        // Comprobamos si es un doble tap dentro del intervalo de tiempo esperado.
                        if (lastTapTimeMs > 0 && (tapTimeMs - lastTapTimeMs) < DOUBLE_TAP_TIME_DELTA && tapCount == 1) {
                            Toast.makeText(getActivity(), "Double tap detected!", Toast.LENGTH_SHORT).show();
                            tapCount = 0; // Reseteamos el contador de taps.
                        } else if (tapCount == 0) {
                            tapCount = 1; // Marcamos el primer tap si no hay un segundo tap esperado.
                        }
                        lastTapTimeMs = tapTimeMs; // Actualizamos el tiempo del último tap.
                    } else if (tapCount == 1 && (currentTime - lastTapTimeMs) >= DOUBLE_TAP_TIME_DELTA) {
                        // Si solo hubo un movimiento y ha pasado el tiempo suficiente desde el último, reseteamos.
                        tapCount = 0;
                    }
                    lastUpdateTime = currentTime;
                    lastZ = z;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Se puede ignorar por ahora
            }
        };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
    }
}
