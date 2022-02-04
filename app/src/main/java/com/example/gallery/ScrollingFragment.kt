

package com.example.gallery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gallery.databinding.FragmentScrollingBinding
import com.google.android.material.snackbar.Snackbar


class ScrollingFragment : Fragment() {
    private lateinit var binding: FragmentScrollingBinding
    private lateinit var photosRecyclerViewAdapter: PhotosRecyclerViewAdapter
    private lateinit var albumRecyclerViewAdapter: AlbumRecyclerViewAdapter
    private lateinit var images: List<String>


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
                isGranted->
            if(isGranted)
            {
                startCamera()
            }
            else{
                Snackbar.make(binding.root,
                    R.string.camera_permission_denied,
                    Snackbar.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_scrolling, container, false)

        binding.cameraFloatingButton.setOnClickListener {
            showCameraPreview()
        }

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sort_menu, menu)
        super.onCreateOptionsMenu(menu,inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.dateModifiedSorting ->
            {
                loadImages(ImagesGallery.SortOrder.Modified)
            }
            R.id.dateSorting ->{
                loadImages(ImagesGallery.SortOrder.Date)

            }
            R.id.nameSorting -> {
                loadImages(ImagesGallery.SortOrder.Name)
            }
            R.id.sizeSorting ->{
                loadImages(ImagesGallery.SortOrder.Size)
            }
        }
        return true
    }

    private fun showCameraPreview() {

        if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
            startCamera()
        }
        else
            requestCameraPermission2()
    }

    private fun requestCameraPermission2() {

        if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
            Snackbar.make(binding.root,
                R.string.permission_required,
                Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok){
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }.show()
        }else
        {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

    }


    private val requestReadPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
                isGranted->
            if(isGranted)
            {
                loadImages(ImagesGallery.SortOrder.Date)
            }
            else{
                Snackbar.make(binding.root,
                    R.string.read_permission_denied,
                    Snackbar.LENGTH_SHORT).show()
            }
        }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showImages()
    }

    private fun showImages() {
        if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            loadImages(ImagesGallery.SortOrder.Name)
        }
        else
            requestReadPermission()

    }

    private fun requestReadPermission() {
        if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
            Snackbar.make(binding.root,
                R.string.permission_required,
                Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok){
                requestReadPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }.show()
        }else
        {
            requestReadPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }


    }

    private fun startCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Check if there exists an Activity to handle the intent, if yes, then send the intent for opening camera
        if (activity?.let { it1 -> intent.resolveActivity(it1.packageManager) } != null) {
            startActivity(intent)
        }
    }



    private fun loadImages(sortOrder: ImagesGallery.SortOrder) {

        Log.i("myTag","This method has been called after permission enabled")
        binding.photosRecyclerView.setHasFixedSize(true)
        val gridLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(requireContext(), 3)
        binding.photosRecyclerView.layoutManager = gridLayoutManager

        images = ImagesGallery.listOfSortedImages(requireContext(),sortOrder)

        val linearLayoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.albumRecyclerView.layoutManager = linearLayoutManager
        albumRecyclerViewAdapter =
            AlbumRecyclerViewAdapter(AlbumConverter.getAlbum(images), object :
                AlbumRecyclerViewAdapter.AlbumListener {
                override fun onAlbumClick(list: List<String>) {
//                    val bundle = Bundle()
//                    bundle.putStringArrayList("photos", ArrayList(list))
//                    PhotosActivity.start(requireContext(), bundle)
                    findNavController().navigate(ScrollingFragmentDirections.actionScrollingFragmentToPhotosFragment(
                        list.toTypedArray()
                    ))
                }
            })
        binding.albumRecyclerView.adapter = albumRecyclerViewAdapter
        photosRecyclerViewAdapter = PhotosRecyclerViewAdapter(requireContext(), images, object :
            PhotosRecyclerViewAdapter.PhotoListener {
            override fun onPhotoClick(position: Int) {
                findNavController().navigate(
                    ScrollingFragmentDirections.actionScrollingFragmentToFullImageFragment(position,
                        images[position]
                    )
                )
            }
        })
        binding.photosRecyclerView.adapter = photosRecyclerViewAdapter
    }



}