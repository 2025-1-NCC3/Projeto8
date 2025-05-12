package br.fecap.pi.ubersafestart.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.fecap.pi.ubersafestart.R;
import br.fecap.pi.ubersafestart.model.Achievement;
import br.fecap.pi.ubersafestart.utils.AchievementTracker;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.ViewHolder> {

    private final List<Achievement> achievements;
    private final Context context;

    public AchievementsAdapter(Context context, List<Achievement> achievements) {
        this.context = context;
        this.achievements = achievements;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);

        holder.title.setText(achievement.getTitle());
        holder.description.setText(achievement.getDescription());
        holder.points.setText("+" + achievement.getPoints());

        // Verificar progresso local para exibição
        boolean isLocallyCompleted = AchievementTracker.isAchievementCompleted(context, achievement.getId());
        int localProgress = AchievementTracker.getAchievementProgress(context, achievement.getId());

        // Se a conquista estiver concluída localmente, sobrescrever o status do objeto
        if (isLocallyCompleted && !achievement.isCompleted()) {
            achievement.setCompleted(true);
            // Definir progresso como alvo
            achievement.setProgress(achievement.getTarget());
        } else if (localProgress > achievement.getProgress()) {
            // Usar o maior progresso
            achievement.setProgress(localProgress);
        }

        // Configurar progresso
        int progressPercentage = achievement.getProgressPercentage();
        holder.progressBar.setProgress(progressPercentage);
        holder.progressText.setText(achievement.getProgress() + "/" + achievement.getTarget());

        // Configurar ícone e estado visual baseado no estado de conclusão
        if (achievement.isCompleted()) {
            // Conquista completada
            holder.completedIcon.setVisibility(View.VISIBLE);
            holder.icon.setImageTintList(ContextCompat.getColorStateList(context, R.color.uber_blue));
            holder.progressBar.setProgressTintList(ContextCompat.getColorStateList(context, R.color.uber_blue));
        } else {
            // Conquista não completada
            holder.completedIcon.setVisibility(View.GONE);
            holder.icon.setImageTintList(ContextCompat.getColorStateList(context, R.color.gray_light));
            holder.progressBar.setProgressTintList(ContextCompat.getColorStateList(context, R.color.gray_medium));
        }

        // Definir ícone apropriado baseado no tipo
        setIconBasedOnType(holder.icon, achievement.getType());
    }

    private void setIconBasedOnType(ImageView imageView, String type) {
        int iconResId;

        switch (type) {
            case "trip":
                iconResId = R.drawable.ic_car;
                break;
            case "checklist":
                iconResId = R.drawable.ic_check_circle;
                break;
            case "share":
                iconResId = R.drawable.ic_share;
                break;
            case "audio":
                iconResId = R.drawable.ic_mic;
                break;
            case "feedback":
                iconResId = R.drawable.ic_star;
                break;
            case "safety":
                iconResId = R.drawable.ic_shield_check;
                break;
            default:
                iconResId = R.drawable.ic_trophy;
                break;
        }

        imageView.setImageResource(iconResId);
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    public void updateAchievements(List<Achievement> newAchievements) {
        achievements.clear();
        achievements.addAll(newAchievements);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView description;
        final TextView points;
        final TextView progressText;
        final ProgressBar progressBar;
        final ImageView icon;
        final ImageView completedIcon;

        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.textViewAchievementTitle);
            description = view.findViewById(R.id.textViewAchievementDescription);
            points = view.findViewById(R.id.textViewAchievementPoints);
            progressText = view.findViewById(R.id.textViewAchievementProgress);
            progressBar = view.findViewById(R.id.progressBarAchievement);
            icon = view.findViewById(R.id.imageViewAchievementIcon);
            completedIcon = view.findViewById(R.id.imageViewAchievementCompleted);
        }
    }
}