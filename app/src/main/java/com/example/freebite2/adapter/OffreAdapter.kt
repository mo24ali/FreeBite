
package com.example.freebite2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.freebite2.databinding.RecyclerItemBinding
import com.example.freebite2.model.OffreModel

class OffreAdapter(private val offreList: List<OffreModel>) : RecyclerView.Adapter<OffreAdapter.OffreViewHolder>() {

    class OffreViewHolder(val binding: RecyclerItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OffreViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RecyclerItemBinding.inflate(inflater, parent, false)
        return OffreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OffreViewHolder, position: Int) {
        val offre = offreList[position]
        holder.binding.offre = offre
        holder.binding.executePendingBindings()
    }

    override fun getItemCount() = offreList.size
}
