package br.fecap.pi.ubersafestart;

import android.os.Bundle;
import android.util.Log; // Import Log
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment; // Use Support Fragment
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.google.android.gms.maps.model.StreetViewSource;

public class FullScreenStreetViewActivity extends AppCompatActivity implements OnStreetViewPanoramaReadyCallback {

    // Tag para os Logs específicos desta Activity
    private static final String TAG = "FullScreenSV";

    private LatLng locationToShow;
    private StreetViewPanorama panorama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_street_view); // Layout da Fase 2

        // Pega as coordenadas enviadas pela activity anterior (DriverHomeActivity)
        double lat = getIntent().getDoubleExtra("latitude", 0.0);
        double lon = getIntent().getDoubleExtra("longitude", 0.0);

        // Log para verificar as coordenadas recebidas
        Log.d(TAG, "onCreate - Recebido Lat: " + lat + ", Lng: " + lon);

        // Valida as coordenadas recebidas
        if (lat == 0.0 && lon == 0.0) {
            // Pode acontecer se o Geocoding falhou na tela anterior e o usuário clicou mesmo assim
            // Ou se houve erro ao passar os extras.
            Log.e(TAG, "onCreate - Coordenadas inválidas ou nulas recebidas!");
            Toast.makeText(this, "Localização inválida para exibir", Toast.LENGTH_LONG).show();
            finish(); // Fecha a activity se não houver coordenadas válidas
            return;
        }
        locationToShow = new LatLng(lat, lon);

        // Encontra o fragmento usando SupportFragmentManager (recomendado para FragmentContainerView)
        FragmentManager fm = getSupportFragmentManager();
        SupportStreetViewPanoramaFragment streetViewFragmentFull =
                (SupportStreetViewPanoramaFragment) fm.findFragmentById(R.id.street_view_panorama_fragment_full);

        // Pede para preparar o StreetView de forma assíncrona
        // O callback onStreetViewPanoramaReady será chamado quando estiver pronto
        if (streetViewFragmentFull != null) {
            Log.d(TAG, "onCreate - Solicitando panorama assíncrono...");
            streetViewFragmentFull.getStreetViewPanoramaAsync(this);
        } else {
            // Erro crítico se o fragmento não for encontrado no layout
            Log.e(TAG, "onCreate - Erro FATAL: Fragmento SupportStreetViewPanoramaFragment não encontrado no layout!");
            Toast.makeText(this, "Erro ao carregar a visualização da rua", Toast.LENGTH_LONG).show();
            finish(); // Fecha a activity
        }

        // Configura o botão de fechar a tela cheia
        ImageButton closeButton = findViewById(R.id.buttonCloseFullScreen);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                Log.d(TAG, "Botão Fechar clicado.");
                finish(); // finish() fecha esta activity e volta para a anterior
            });
        } else {
            Log.e(TAG, "onCreate - Erro: Botão Fechar (buttonCloseFullScreen) não encontrado no layout!");
        }
    }

    
    @Override
    public void onStreetViewPanoramaReady(@NonNull StreetViewPanorama streetViewPanorama) {
        Log.d(TAG, "onStreetViewPanoramaReady - Panorama está pronto.");
        this.panorama = streetViewPanorama;

        // Verifica se temos uma localização válida para mostrar
        if (locationToShow != null) {
            // Log para verificar a posição sendo definida
            Log.d(TAG, "onStreetViewPanoramaReady - Definindo posição do panorama para: " + locationToShow);

            // --- Configurações do Panorama em Tela Cheia ---
            // Define a localização. Raio maior (150m) e prioriza OUTDOOR para ter mais chance de achar algo.
            panorama.setPosition(locationToShow, 150, StreetViewSource.OUTDOOR);

            // Habilita a INTERAÇÃO do usuário
            panorama.setUserNavigationEnabled(true); // Permite "andar" pelas setas
            panorama.setPanningGesturesEnabled(true); // Permite arrastar para olhar em volta (pan)
            panorama.setZoomGesturesEnabled(true); // Permite fazer zoom com os dedos
            panorama.setStreetNamesEnabled(true); // Mostra nomes de rua sobrepostos

            // Log para confirmar que a interação foi habilitada
            Log.d(TAG, "onStreetViewPanoramaReady - Interação do usuário HABILITADA (Navegação, Panning, Zoom, Nomes de Rua)");

            // Adiciona um Listener para ser notificado se a posição realmente carregar um panorama
            // ou se não encontrar nada para o local específico.
            panorama.setOnStreetViewPanoramaChangeListener(location -> {
                if (location == null || location.links == null) {
                    // Não encontrou imagem do Street View para esta coordenada específica
                    Log.w(TAG, "StreetViewPanoramaChangeListener - Nenhum panorama encontrado para este local: " + locationToShow);
                    // Informa o usuário que o local não tem Street View disponível
                    Toast.makeText(FullScreenStreetViewActivity.this,
                            "Street View não disponível para este local exato",
                            Toast.LENGTH_SHORT).show();
                    // Poderia tentar novamente sem restrições, mas pode mostrar um local distante:
                    // panorama.setPosition(locationToShow);
                } else {
                    // Encontrou e carregou um panorama válido
                    Log.d(TAG, "StreetViewPanoramaChangeListener - Panorama válido carregado para: " + location.position);
                    // Poderia fazer algo aqui se necessário, mas geralmente não precisa
                }
            });

        } else {
            // Isso não deveria acontecer por causa da validação no onCreate, mas é uma segurança extra.
            Log.e(TAG, "onStreetViewPanoramaReady - Erro: locationToShow é nula no momento do callback!");
            Toast.makeText(this, "Erro: Localização inválida ao carregar Street View", Toast.LENGTH_LONG).show();
            finish(); // Fecha se a localização for nula
        }
    }
}
