package com.example.moviecollection;

import android.app.Activity;
import android.app.ListFragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MovieListFragment extends ListFragment
{
   // callback methods implemented by MainActivity  
   public interface MovieListFragmentListener
   {
      // called when user selects a movie
      public void onMovieSelected(long rowID);

      // called when user decides to add a movie
      public void onAddMovie();
   }
   
   private MovieListFragmentListener listener; 
   
   private ListView movieListView; // the ListActivity's ListView
   private CursorAdapter movieAdapter; // adapter for ListView
   
   // set movieListFragmentListener when fragment attached   
   @Override
   public void onAttach(Activity activity)
   {
      super.onAttach(activity);
      listener = (MovieListFragmentListener) activity;
   }

   // remove movieListFragmentListener when Fragment detached
   @Override
   public void onDetach()
   {
      super.onDetach();
      listener = null;
   }

   // called after View is created
   @Override
   public void onViewCreated(View view, Bundle savedInstanceState)
   {
      super.onViewCreated(view, savedInstanceState);
      setRetainInstance(true); // save fragment across config changes
      setHasOptionsMenu(true); // this fragment has menu items to display

      // set text to display when there are no movies
      setEmptyText(getResources().getString(R.string.no_movies));

      // get ListView reference and configure ListView
      movieListView = getListView(); 
      movieListView.setOnItemClickListener(viewMovieListener);      
      movieListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
      
      // map each movie's name to a TextView in the ListView layout
      String[] from = new String[] { "title" };
      int[] to = new int[] { android.R.id.text1 };
      movieAdapter = new SimpleCursorAdapter(getActivity(), 
         android.R.layout.simple_list_item_1, null, from, to, 0);
      setListAdapter(movieAdapter); // set adapter that supplies data
   }

   // responds to the user touching a movie's name in the ListView
   OnItemClickListener viewMovieListener = new OnItemClickListener() 
   {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, 
         int position, long id) 
      {
         listener.onMovieSelected(id); // pass selection to MainActivity
      } 
   }; // end viewmovieListener

   // when fragment resumes, use a GetmoviesTask to load movies 
   @Override
   public void onResume() 
   {
      super.onResume(); 
      new GetMoviesTask().execute((Object[]) null);
   }

   // performs database query outside GUI thread
   private class GetMoviesTask extends AsyncTask<Object, Object, Cursor> 
   {
      DatabaseConnector databaseConnector = 
         new DatabaseConnector(getActivity());

      // open database and return Cursor for all movies
      @Override
      protected Cursor doInBackground(Object... params)
      {
         databaseConnector.open();
         return databaseConnector.getAllMovies(); 
      } 

      // use the Cursor returned from the doInBackground method
      @Override
      protected void onPostExecute(Cursor result)
      {
         movieAdapter.changeCursor(result); // set the adapter's Cursor
         databaseConnector.close();
      } 
   } // end class GetmoviesTask

   // when fragment stops, close Cursor and remove from movieAdapter 
   @Override
   public void onStop() 
   {
      Cursor cursor = movieAdapter.getCursor(); // get current Cursor
      movieAdapter.changeCursor(null); // adapter now has no Cursor
      
      if (cursor != null) 
         cursor.close(); // release the Cursor's resources
      
      super.onStop();
   } 

   // display this fragment's menu items
   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.fragment_movie_list_menu, menu);
   }

   // handle choice from options menu
   @Override
   public boolean onOptionsItemSelected(MenuItem item) 
   {
      switch (item.getItemId())
      {
         case R.id.action_add:
            listener.onAddMovie();
            return true;
      }
      
      return super.onOptionsItemSelected(item); // call super's method
   }
   
   // update data set
   public void updateMovieList()
   {
      new GetMoviesTask().execute((Object[]) null);
   }
}