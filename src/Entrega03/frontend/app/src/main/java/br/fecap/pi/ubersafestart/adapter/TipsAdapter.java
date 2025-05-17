package br.fecap.pi.ubersafestart.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.fecap.pi.ubersafestart.R;
import br.fecap.pi.ubersafestart.model.SafetyTip;

public class TipsAdapter extends RecyclerView.Adapter<TipsAdapter.ViewHolder> {

    private final List<SafetyTip> tips;
    private final Context context;

    public TipsAdapter(Context context, List<SafetyTip> tips) {
        this.context = context;
        this.tips = tips;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_safety_tip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SafetyTip tip = tips.get(position);

        holder.title.setText(tip.getTitle());
        holder.shortDescription.setText(tip.getShortDescription());
        holder.longDescription.setText(tip.getLongDescription());
        holder.icon.setImageResource(tip.getIconResId());

        // Definir visibilidade com base no estado de expansão
        holder.longDescription.setVisibility(tip.isExpanded() ? View.VISIBLE : View.GONE);

        // Rotacionar ícone de expansão se expandido
        holder.expandIcon.setRotation(tip.isExpanded() ? 180 : 0);

        // Configurar clique para expandir/contrair
        holder.itemView.setOnClickListener(v -> {
            boolean wasExpanded = tip.isExpanded();
            tip.toggleExpanded();

            // Animar a rotação do ícone
            if (wasExpanded) {
                holder.expandIcon.animate().rotation(0).setDuration(300).start();
                holder.longDescription.setVisibility(View.GONE);
            } else {
                holder.expandIcon.animate().rotation(180).setDuration(300).start();
                holder.longDescription.setVisibility(View.VISIBLE);

                // Animar a expansão
                Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);
                holder.longDescription.startAnimation(slideDown);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tips.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView shortDescription;
        final TextView longDescription;
        final ImageView icon;
        final ImageView expandIcon;

        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.textViewTipTitle);
            shortDescription = view.findViewById(R.id.textViewTipShortDescription);
            longDescription = view.findViewById(R.id.textViewTipLongDescription);
            icon = view.findViewById(R.id.imageViewTipIcon);
            expandIcon = view.findViewById(R.id.imageViewExpandIcon);
        }
    }
}