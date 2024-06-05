//import android.R
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import androidx.recyclerview.widget.RecyclerView
//
//internal class ViewPager2Adapter (private val ctx: Context) : RecyclerView.Adapter<ViewPager2Adapter.ViewHolder>() {
//    // Array of images
//    // Adding images from drawable folder
//    val dateArray: ArrayList<ViewPagerItem> = ArrayList()
//
//    // This method returns our layout
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view: View = LayoutInflater.from(ctx).inflate(R.layout.images_holder, parent, false)
//        return ViewHolder(view)
//    }
//
//    // This method binds the screen with the view
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        // This will set the images in imageview
//        holder.images.setImageResource(images[position])
//    }
//
//    // This Method returns the size of the Array
//    override fun getItemCount(): Int {
//        return images.size
//    }
//
//    // The ViewHolder class holds the view
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        var images: ImageView = itemView.findViewById<ImageView>(R.id.images)
//    }
//}