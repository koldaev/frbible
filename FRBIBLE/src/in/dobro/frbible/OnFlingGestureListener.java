package in.dobro.frbible;

import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public abstract class OnFlingGestureListener implements OnTouchListener {


  @SuppressWarnings("deprecation")
private final GestureDetector gdt = new GestureDetector(new GestureListener());

  @Override
  public boolean onTouch(final View v, final MotionEvent event) {
	  gdt.onTouchEvent(event);
	  return true;
  }

  private final class GestureListener extends SimpleOnGestureListener {

     private static final int SWIPE_MIN_DISTANCE = 60;
     private static final int SWIPE_THRESHOLD_VELOCITY = 100;

     @Override
     public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
           onRightToLeft();
           return true;
        } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
           onLeftToRight();
           return true;
        }
        if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
           onBottomToTop();
           return true;
        } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
           onTopToBottom();
           return true;
        }
        return false;
     }
  }

  public abstract void onRightToLeft();

  public abstract void onLeftToRight();

  public abstract void onBottomToTop();

  public abstract void onTopToBottom();

}
