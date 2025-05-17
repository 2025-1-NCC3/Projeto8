package br.fecap.pi.ubersafestart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.fecap.pi.ubersafestart.adapter.TipsAdapter;
import br.fecap.pi.ubersafestart.model.SafetyTip;

public class TipsActivity extends AppCompatActivity {

    private static final String TAG = "TipsActivity";
    private RecyclerView recyclerViewTips;
    private LinearLayout navHome, navTips, navAchievements, navAccount;
    private ImageView iconHome, iconTips, iconAchievements, iconAccount;
    private TextView textHome, textTips, textAchievements, textAccount;

    // Array de IDs para facilitar a atualização da barra de navegação
    private final int[] navIconIds = {R.id.iconHome, R.id.iconTips, R.id.iconAchievements, R.id.iconAccount};
    private final int[] navTextIds = {R.id.textHome, R.id.textTips, R.id.textAchievements, R.id.textAccount};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips);

        initViews();
        setupNavigationListeners();
        updateBottomNavigationSelection(R.id.navTips);

        // Inicializar e configurar a RecyclerView
        recyclerViewTips = findViewById(R.id.recyclerViewTips);
        recyclerViewTips.setLayoutManager(new LinearLayoutManager(this));

        // Criar lista de dicas de segurança
        List<SafetyTip> safetyTips = createSafetyTips();

        // Configurar adaptador
        TipsAdapter adapter = new TipsAdapter(this, safetyTips);
        recyclerViewTips.setAdapter(adapter);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbarTips);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> navigateToHome());

        // Inicializar componentes da navegação
        navHome = findViewById(R.id.navHome);
        navTips = findViewById(R.id.navTips);
        navAchievements = findViewById(R.id.navAchievements);
        navAccount = findViewById(R.id.navAccount);

        iconHome = findViewById(R.id.iconHome);
        textHome = findViewById(R.id.textHome);
        iconTips = findViewById(R.id.iconTips);
        textTips = findViewById(R.id.textTips);
        iconAchievements = findViewById(R.id.iconAchievements);
        textAchievements = findViewById(R.id.textAchievements);
        iconAccount = findViewById(R.id.iconAccount);
        textAccount = findViewById(R.id.textAccount);
    }

    private void setupNavigationListeners() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            if (id == R.id.navHome) {
                navigateToHome();
            } else if (id == R.id.navTips) {
                // Já está na tela de dicas
                updateBottomNavigationSelection(R.id.navTips);
                Toast.makeText(TipsActivity.this, "Você já está na tela de Dicas", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.navAchievements) {
                Intent intent = new Intent(TipsActivity.this, AchievementsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else if (id == R.id.navAccount) {
                Intent intent = new Intent(TipsActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        };

        if (navHome != null) navHome.setOnClickListener(listener);
        if (navTips != null) navTips.setOnClickListener(listener);
        if (navAchievements != null) navAchievements.setOnClickListener(listener);
        if (navAccount != null) navAccount.setOnClickListener(listener);
    }

    private void navigateToHome() {
        SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        String userType = prefs.getString("type", "");
        Intent intent;

        if ("driver".equalsIgnoreCase(userType)) {
            intent = new Intent(TipsActivity.this, DriverHomeActivity.class);
        } else {
            intent = new Intent(TipsActivity.this, HomeActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    private void updateBottomNavigationSelection(int selectedItemId) {
        LinearLayout[] navItems = {navHome, navTips, navAchievements, navAccount};
        ImageView[] navIcons = {iconHome, iconTips, iconAchievements, iconAccount};
        TextView[] navTexts = {textHome, textTips, textAchievements, textAccount};

        int activeColor = ContextCompat.getColor(this, R.color.white_fff);
        int inactiveColor = ContextCompat.getColor(this, R.color.gray_light);

        for (int i = 0; i < navItems.length; i++) {
            LinearLayout itemLayout = navItems[i];
            ImageView icon = navIcons[i];
            TextView text = navTexts[i];

            if (itemLayout == null || icon == null || text == null) {
                continue;
            }

            boolean isActive = (itemLayout.getId() == selectedItemId);
            icon.setImageTintList(ColorStateList.valueOf(isActive ? activeColor : inactiveColor));
            text.setTextColor(isActive ? activeColor : inactiveColor);
        }
    }

    private List<SafetyTip> createSafetyTips() {
        List<SafetyTip> tips = new ArrayList<>();

        // Adicionar todas as dicas de segurança fornecidas
        tips.add(new SafetyTip(
                "Central de Segurança",
                "Um lugar central para acessar todas as ferramentas de segurança",
                "A Central de Segurança é um local centralizado no aplicativo onde você pode acessar todas as principais ferramentas de segurança disponíveis, como compartilhamento de viagem, contatos de confiança, ligação para polícia e todas as outras funcionalidades de segurança. Ela é identificada por um ícone de escudo no aplicativo.",
                R.drawable.ic_shield_check
        ));

        tips.add(new SafetyTip(
                "Compartilhamento de Viagem (U-Acompanha)",
                "Compartilhe sua localização em tempo real com contatos de confiança",
                "O U-Acompanha permite que você compartilhe os detalhes da sua viagem em tempo real com contatos de confiança, que podem acompanhar seu trajeto pelo mapa até o fim da viagem. É possível configurar para compartilhar todas as viagens automaticamente ou apenas em determinados horários ou locais. Para ativar, toque em \"Compartilhar status da viagem\" durante uma corrida ou configure nas preferências de segurança.",
                R.drawable.ic_share
        ));

        tips.add(new SafetyTip(
                "U-Ajuda (Checagem de Rota)",
                "Monitoramento automático de desvios de rota ou paradas inesperadas",
                "O U-Ajuda utiliza GPS e outros sensores para identificar paradas inesperadas, desvios de rota ou término de viagem antes do previsto. Nesses casos, o aplicativo pode enviar mensagens proativas tanto para o motorista quanto para o passageiro oferecendo suporte e direcionando para as ferramentas de segurança disponíveis. Esta funcionalidade ajuda a garantir que tudo esteja correndo bem durante a viagem.",
                R.drawable.ic_map
        ));

        tips.add(new SafetyTip(
                "U-Código (Verificação com PIN)",
                "Confirme que está no veículo certo com um código de 4 dígitos",
                "O U-Código gera um código PIN único de quatro dígitos para cada viagem. Este código deve ser fornecido pelo passageiro ao motorista para que a viagem possa ser iniciada no aplicativo. Esta verificação ajuda a garantir que o passageiro entre no veículo correto e aumenta a segurança para ambas as partes. É especialmente útil em locais com muitos carros de aplicativo, como aeroportos e eventos.",
                R.drawable.ic_lock
        ));

        tips.add(new SafetyTip(
                "Gravação de Áudio (U-Áudio)",
                "Grave o áudio da viagem diretamente pelo aplicativo",
                "O U-Áudio permite que tanto passageiros quanto motoristas gravem o áudio das viagens diretamente pelo aplicativo. Esta função pode ser ativada a qualquer momento durante a corrida. O conteúdo é armazenado de forma criptografada e pode ser enviado para a Uber em caso de incidentes, para ser utilizado em investigações se necessário. A gravação só é iniciada mediante o consentimento do usuário.",
                R.drawable.ic_mic
        ));

        tips.add(new SafetyTip(
                "Ligar para a Polícia (U-Help)",
                "Acesse rapidamente os serviços de emergência durante a viagem",
                "O U-Help é um botão de emergência no aplicativo que permite ligar diretamente para os serviços de segurança locais (190 no Brasil) em caso de emergência. Em algumas localidades, as informações da viagem e da localização podem ser enviadas automaticamente às autoridades para facilitar o atendimento. Esta função está disponível na tela de viagem, dentro do menu de segurança.",
                R.drawable.ic_phone
        ));

        tips.add(new SafetyTip(
                "Contatos de Confiança",
                "Cadastre pessoas para compartilhar sua viagem rapidamente",
                "Você pode cadastrar contatos de confiança no aplicativo para compartilhar suas viagens de forma rápida e automática. Para configurar, acesse o menu de perfil, vá em \"Segurança\" e depois em \"Contatos de Confiança\". Uma vez configurados, você pode compartilhar sua localização em tempo real com essas pessoas durante as viagens com apenas alguns toques.",
                R.drawable.ic_account
        ));

        tips.add(new SafetyTip(
                "U-Check (Verificação de Usuários)",
                "Sistema de verificação de identidade para maior segurança",
                "O U-Check é um sistema que realiza checagens de segurança nos perfis de motoristas parceiros (incluindo checagem de apontamentos criminais, nos termos da lei, e verificação de identidade em tempo real por selfie) e também dos usuários (verificação via cartão de crédito, CPF e data de nascimento). Para pagamentos em dinheiro, pode ser solicitada uma foto do documento de identidade como medida adicional de segurança.",
                R.drawable.ic_check_circle
        ));

        tips.add(new SafetyTip(
                "U-Sigilo (Telefone Anônimo)",
                "Comunicação sem revelar seu número real de telefone",
                "Com o U-Sigilo, os números de telefone de passageiros e motoristas são mantidos em sigilo. Toda a comunicação ocorre através de um servidor da Uber, que atua como intermediário, protegendo a privacidade de ambas as partes. Assim, quando você liga ou envia mensagem para o motorista pelo aplicativo, seu número real não é compartilhado, e vice-versa.",
                R.drawable.ic_phone
        ));

        tips.add(new SafetyTip(
                "U-Filtro (Detecção de Mensagens Inapropriadas)",
                "Tecnologia que filtra mensagens potencialmente ofensivas",
                "O chat no aplicativo conta com o U-Filtro, uma tecnologia que identifica mensagens que possam ser consideradas ofensivas ou ameaçadoras. Mensagens sinalizadas são encaminhadas para revisão e podem levar à desativação da conta do usuário se confirmadas como inadequadas. Esta ferramenta ajuda a manter um ambiente de comunicação seguro e respeitoso para todos.",
                R.drawable.ic_message
        ));

        tips.add(new SafetyTip(
                "Seguro para Acidentes Pessoais",
                "Cobertura de seguro para imprevistos durante a viagem",
                "Todas as viagens realizadas pela plataforma contam com um seguro para acidentes pessoais para motoristas e passageiros. Esta cobertura inclui despesas médicas e outras eventualidades, dentro dos termos específicos da apólice. O seguro é ativado automaticamente quando a viagem é iniciada pelo aplicativo, sem custo adicional para os usuários.",
                R.drawable.ic_shield_check
        ));

        tips.add(new SafetyTip(
                "Preferências de Segurança",
                "Configure ferramentas de segurança para ativação automática",
                "As Preferências de Segurança permitem configurar a ativação automática de determinadas funcionalidades de segurança em situações específicas. Por exemplo, você pode ativar o compartilhamento de localização e o U-Ajuda para viagens noturnas ou próximas a bares e restaurantes. Para configurar, acesse o menu de perfil e vá em \"Segurança\" > \"Preferências de Segurança\".",
                R.drawable.ic_settings
        ));

        tips.add(new SafetyTip(
                "Sistema de Avaliações",
                "Avaliações mútuas para manter a qualidade e segurança",
                "O Sistema de Avaliações permite que passageiros e motoristas se avaliem mutuamente após cada viagem. Este sistema contribui para a qualidade e segurança da comunidade, pois medidas podem ser tomadas em casos de baixa avaliação ou comportamento inadequado. As avaliações são anônimas e ajudam a manter o padrão de qualidade do serviço.",
                R.drawable.ic_star
        ));

        tips.add(new SafetyTip(
                "Verificação de Veículo",
                "Confirme os dados do veículo antes de embarcar",
                "Sempre verifique se as informações do carro (modelo, cor e placa) e a foto do motorista exibidas no aplicativo correspondem ao veículo e condutor antes de iniciar a viagem. Esta verificação é fundamental para sua segurança e evita entrar no carro errado. Em caso de divergência, não entre no veículo e reporte o problema pelo aplicativo.",
                R.drawable.ic_car
        ));

        tips.add(new SafetyTip(
                "Viagem para Convidados",
                "Solicite uma viagem para outra pessoa com segurança",
                "A função Viagem para Convidados permite pedir uma viagem para outra pessoa. A pessoa que vai utilizar o serviço receberá as informações do carro e motorista por SMS, aumentando a segurança para quem será buscado. Esta função é ideal para familiares, amigos ou pessoas que não têm o aplicativo instalado, garantindo que eles também tenham acesso às informações necessárias para uma viagem segura.",
                R.drawable.ic_person
        ));

        tips.add(new SafetyTip(
                "Bloqueio de Usuários (para motoristas)",
                "Motoristas podem escolher não receber solicitações de determinados passageiros",
                "Motoristas parceiros têm a opção de bloquear passageiros com avaliações baixas para evitar futuras viagens. Esta funcionalidade dá mais autonomia aos motoristas e contribui para um ambiente mais seguro e harmonioso para todos os usuários da plataforma. Para ativar, o motorista pode acessar as configurações de preferência de viagem no aplicativo.",
                R.drawable.ic_block
        ));

        return tips;
    }

    @Override
    public void onBackPressed() {
        navigateToHome();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBottomNavigationSelection(R.id.navTips);
    }

    /**
     * Método estático para adicionar a opção de Dicas na barra de navegação das activity principal e do motorista.
     * Deve ser chamado na onCreate das activities Home e DriverHome.
     */
    public static void addTipsOptionToNavbar(Context context, LinearLayout navServicesOrEarnings) {
        if (navServicesOrEarnings != null) {
            navServicesOrEarnings.setOnClickListener(v -> {
                Intent intent = new Intent(context, TipsActivity.class);
                context.startActivity(intent);
                if (context instanceof Activity) {
                    ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        }
    }
}