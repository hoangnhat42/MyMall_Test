package com.project.scan_on;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.google.android.material.card.MaterialCardView;

public  class  MaterialCardTranslation  extends MaterialCardView {

    private  boolean isElevated =  false ;
    private  int  ANIM_DURATION  =  250 ;

    public  MaterialCardTranslation ( Context context ) {
        super (context);
    }

    public  MaterialCardTranslation ( Context  context , AttributeSet attrs ) {
        super (context, attrs);
    }

    public  MaterialCardTranslation ( Context  context , AttributeSet  attrs , int  defStyleAttr ) {
        super (context, attrs, defStyleAttr);
    }

    public  boolean  isElevated () {
        return isElevated;
    }

    public  void  setDuration ( final  int  duration ) {
        ANIM_DURATION  = duration;
    }

    public  void  elevateCard ( final  boolean  elevate , @Nullable final  EndAction  endAction ) {
        dispatchElevateAnim (elevate, endAction);

    }

    public  void  simulateClickElevation ( @Nullable  final  EndAction  endAction ) {
        dispatchElevateCompleteAnim (endAction);
    }

    private  void  dispatchElevateAnim ( final  boolean  elevate , @Nullable  final  EndAction  endAction ) {
        if (elevate) {
            upAnim ();
        } else {
            downAnim ();
        }

        isElevated = elevate;

        new  Handler () . postDelayed ( new  Runnable () {
            @Override
            public  void  run () {
                endAction . onEnd ();
            }
        }, ANIM_DURATION );

    }

    private  void  dispatchElevateCompleteAnim ( @Nullable  final  EndAction  endAction ) {
        if (isElevated || isLessThan21 ())
            return ;

        final  float posZ = getZ();

        animate () . translationZ (posZ *  4 ) . setDuration ( ANIM_DURATION )
                .setInterpolator ( new  FastOutSlowInInterpolator ())
                .withEndAction ( new  Runnable () {
                    @Override
                    public  void  run () {
                        downAnim ();
                    }
                });

        if (endAction !=  null)
        new Handler() . postDelayed (new  Runnable () {
            @Override
            public  void  run () {
                endAction . onEnd ();
            }
        }, ANIM_DURATION  *  2 );
    }

    private  void  upAnim () {
        if (isLessThan21 ())
            return ;
        animate () . translationZ (getZ() *  4 ) . setDuration ( ANIM_DURATION )
                .setInterpolator ( new FastOutSlowInInterpolator())
                .start ();

    }

    private  void  downAnim () {
        if (isLessThan21 ())
            return ;
        animate() . translationZ ( 0 ) . setDuration ( ANIM_DURATION )
                .setInterpolator ( new  FastOutSlowInInterpolator ())
                .start ();
    }

    public  interface  EndAction {
        void  onEnd();
    }

    private  boolean  isLessThan21 () {
        return  Build. VERSION . SDK_INT  <  Build . VERSION_CODES . LOLLIPOP ;
    }

}