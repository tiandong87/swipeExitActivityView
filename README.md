# SwipeExitActivityView
Android Lib SwipeExitActivity.

# Not Last Activity 
<pre>
 @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        new SwipeExitActivityLayout(this);
    }
</pre>

# Has Last Activity 

<pre>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);
            Bitmap perActivitybackground = null;
            View view = LastActivity.getWindow().getDecorView();
            view.invalidate();
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            perActivitybackground = view.getDrawingCache();
        new SwipeExitActivityLayout(this,perActivitybackground);
    }
</pre>

