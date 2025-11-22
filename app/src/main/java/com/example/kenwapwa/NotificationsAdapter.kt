package com.example.kenwapwa


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationsAdapter(
    private val notifications: MutableList<Notification>
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tv_notif_title)
        val message: TextView = view.findViewById(R.id.tv_notif_message)
        val time: TextView = view.findViewById(R.id.tv_notif_time)
        val typeLabel: TextView = view.findViewById(R.id.tv_notif_type)
        val redDot: CardView = view.findViewById(R.id.cv_red_dot)
        val btnMarkRead: TextView = view.findViewById(R.id.btn_mark_read)
        val cardContainer: View = view.findViewById(R.id.card_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = notifications[position]

        holder.title.text = item.title
        holder.message.text = item.message

        // Format Date (Simple parser, assumes ISO string)
        holder.time.text = item.created_at?.take(10) ?: "Just Now"

        // Logic for Type Label
        if (item.recipient_type == "all_waste_pickers") {
            holder.typeLabel.text = "• Global Alert"
        } else {
            holder.typeLabel.text = "• ${item.recipient_type} Update"
        }

        // Handle Read/Unread Dot
        if (item.isRead) {
            holder.redDot.visibility = View.GONE
            holder.btnMarkRead.visibility = View.GONE
        } else {
            holder.redDot.visibility = View.VISIBLE
            holder.btnMarkRead.visibility = View.VISIBLE
        }

        // Handle Text Expansion
        if (item.isExpanded) {
            holder.message.maxLines = Int.MAX_VALUE
        } else {
            holder.message.maxLines = 2
        }

        // Click Listeners
        holder.cardContainer.setOnClickListener {
            item.isExpanded = !item.isExpanded
            notifyItemChanged(position)
        }

        holder.btnMarkRead.setOnClickListener {
            item.isRead = true
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = notifications.size

    fun markAllRead() {
        notifications.forEach { it.isRead = true }
        notifyDataSetChanged()
    }

    fun updateData(newLists: List<Notification>) {
        notifications.clear()
        notifications.addAll(newLists)
        notifyDataSetChanged()
    }
}