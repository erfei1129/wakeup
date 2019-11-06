package com.lskj.wakeup.test;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.util.Log;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Ge Xiaodong
 * @time 2019/7/23 17:52
 * @description
 */
public class MediaPlayUtil {
    private static MediaPlayer mediaPlayer;
    private static MediaPlayer rawMediaPlayer;
    private static MediaPlayer mLocalPlayer;

    @SuppressLint("CheckResult")
    public static void startRemotePlay(String audioPath) {
        Single.just(audioPath)
                .map((Function<String, String>) s -> {
                    try {
                        if (null != mediaPlayer) {
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.stop();
                            }
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.reset();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setOnPreparedListener(MediaPlayer::start);
                        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
//                            AppUtils.showToastSafe("音频无法播放");
                            Log.e("MediaPlayUtil-error", "音频无法播放");
                            return true;
                        });
                        mediaPlayer.setOnCompletionListener(player -> {
                            if (null != mediaPlayer) {
                                mediaPlayer.release();
                                mediaPlayer = null;
                                if (onRemotePlayCompleteListener != null) {
                                    onRemotePlayCompleteListener.onPlayComplete(audioPath);
                                }
                            }
                        });
                        mediaPlayer.setDataSource(audioPath);
                        mediaPlayer.prepare();
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (null != mediaPlayer) {
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.stop();
                            }
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                    }
                    return null;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(s -> {
                }, Throwable::printStackTrace);
    }

    public static void stopRemotePlayer() {
        if (null != mediaPlayer) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @SuppressLint("CheckResult")
    public static void startRawPlay(Context context, @IdRes int resId) {
        Single.just(resId + "")
                .map((Function<String, String>) s -> {
                    if (null != rawMediaPlayer) {
                        if (rawMediaPlayer.isPlaying()) {
                            rawMediaPlayer.stop();
                        }
                        rawMediaPlayer.release();
                        rawMediaPlayer = null;
                    }
                    try {
                        rawMediaPlayer = MediaPlayer.create(context, resId);
                        rawMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        rawMediaPlayer.setOnErrorListener((mp, what, extra) -> {
                            Log.e("MediaPlayUtil-error", "音频无法播放");
                            return true;
                        });
                        rawMediaPlayer.setOnCompletionListener(player -> {
                            if (null != rawMediaPlayer) {
                                rawMediaPlayer.release();
                                rawMediaPlayer = null;

                                if (onRawPlayCompleteListener != null) {
                                    onRawPlayCompleteListener.onPlayComplete();
                                }
                            }
                        });
                        rawMediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (null != rawMediaPlayer) {
                            if (rawMediaPlayer.isPlaying()) {
                                rawMediaPlayer.stop();
                            }
                            rawMediaPlayer.release();
                            rawMediaPlayer = null;
                        }
                    }
                    return null;
                }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(s -> {
                }, Throwable::printStackTrace);
    }

    public static void stopRawPlayer() {
        if (null != rawMediaPlayer) {
            if (rawMediaPlayer.isPlaying()) {
                rawMediaPlayer.stop();
            }
            rawMediaPlayer.release();
            rawMediaPlayer = null;
        }
    }

    public static void pausePlayer() {
        if (null != mediaPlayer) {
            mediaPlayer.pause();
            mediaPlayer = null;
        }
    }

    public static void seekTo(int time) {
        if (null != mediaPlayer) {
            mediaPlayer.seekTo(time);
        }
    }

    /**
     * 播放本地sd卡音频
     *
     * @param context
     * @param localPath
     */
    @SuppressLint("CheckResult")
    public static void startLocalPlay(Context context, String localPath) {
        Single.just("")
                .map((Function<String, String>) s -> {
                    if (null != mLocalPlayer) {
                        if (mLocalPlayer.isPlaying()) {
                            mLocalPlayer.stop();
                        }
                        mLocalPlayer.release();
                        mLocalPlayer = null;
                    }
                    try {
                        mLocalPlayer = new MediaPlayer();
                        mLocalPlayer = MediaPlayer.create(context, Uri.parse(localPath));
                        mLocalPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mLocalPlayer.setOnErrorListener((mp, what, extra) -> {
                            Log.e("MediaPlayUtil-error", "音频无法播放");
                            return true;
                        });
                        mLocalPlayer.setOnCompletionListener(player -> {
                            if (null != mLocalPlayer) {
                                mLocalPlayer.release();
                                mLocalPlayer = null;
                                if (onLocalPlayCompleteListener != null) {
                                    onLocalPlayCompleteListener.onPlayComplete(localPath);
                                }
                            }
                        });
                        mLocalPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (null != mLocalPlayer) {
                            if (mLocalPlayer.isPlaying()) {
                                mLocalPlayer.stop();
                            }
                            mLocalPlayer.release();
                            mLocalPlayer = null;
                        }
                    }
                    return null;
                }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(s -> {
                }, Throwable::printStackTrace);
    }

    public static boolean isLocalPlaying() {
        if (null != mLocalPlayer) {
            return mLocalPlayer.isPlaying();
        }
        return false;
    }


    public static void stopLocalPlayer() {
        if (null != mLocalPlayer) {
            if (mLocalPlayer.isPlaying()) {
                mLocalPlayer.stop();
            }
            mLocalPlayer.release();
            mLocalPlayer = null;
        }
    }


    private static OnRemotePlayComplete onRemotePlayCompleteListener;

    public static void setRemoteCompleteListener(OnRemotePlayComplete listener) {
        onRemotePlayCompleteListener = listener;
    }

    public interface OnRemotePlayComplete {
        void onPlayComplete(String path);
    }

    private static OnLocalPlayComplete onLocalPlayCompleteListener;

    public static void setLocalCompleteListener(OnLocalPlayComplete listener) {
        onLocalPlayCompleteListener = listener;
    }

    public interface OnLocalPlayComplete {
        void onPlayComplete(String path);
    }

    private static OnRawPlayComplete onRawPlayCompleteListener;

    public static void setRawCompleteListener(OnRawPlayComplete listener) {
        onRawPlayCompleteListener = listener;
    }

    public interface OnRawPlayComplete {
        void onPlayComplete();
    }

}
