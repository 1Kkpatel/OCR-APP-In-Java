package com.example.ocrinkotlin;

import static com.example.ocrinkotlin.R.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ExtractedTextPage extends AppCompatActivity {

    private Button btnIntent;
    private Button btnSelect;
    private Button btnChoose;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.extracted_text_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainB), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = new Intent(this, MainActivity.class);

        // Add the letter as extra data
        intent.putExtra("letter", btnIntent.getText().toString());

        // Start the DetailActivity
        startActivity(intent);


        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }

            private void selectImage() {
            }
        });




    }
}