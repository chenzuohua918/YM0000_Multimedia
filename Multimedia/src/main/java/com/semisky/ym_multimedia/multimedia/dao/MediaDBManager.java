package com.semisky.ym_multimedia.multimedia.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.semisky.ym_multimedia.common.utils.Logger;
import com.semisky.ym_multimedia.multimedia.utils.FileUriUtil;
import com.semisky.ym_multimedia.multimedia.utils.MultimediaConstants;
import com.semisky.ym_multimedia.multimedia.utils.UsbStateManager;

/**
 * 多媒体数据库操作类
 *
 * @author Anter
 */
public class MediaDBManager {
    private static MediaDBManager instance;
    private MediaDBHelper mDBHelper;
    private SQLiteDatabase writableDB;
    private List<ContentValues> valuesList1, valuesList2;// 暂存List集合
    private int capacity = 2500;// 分批插入时一批多少个

    private MediaDBManager(Context context) {
        mDBHelper = new MediaDBHelper(context, DBConfiguration.DATABASE_NAME, null,
                DBConfiguration.DATABASE_VERSION);
        valuesList1 = new ArrayList<ContentValues>(capacity);
        valuesList2 = new ArrayList<ContentValues>(capacity);
        writableDB = mDBHelper.getWritableDatabase();
        // 设置数据库默认语言（用于排序）
        writableDB.setLocale(Locale.getDefault());
    }

    private static synchronized void syncInit(Context context) {
        if (instance == null) {
            instance = new MediaDBManager(context);
        }
    }

    public static MediaDBManager getInstance(Context context) {
        if (instance == null) {
            syncInit(context);
        }
        return instance;
    }

    private synchronized SQLiteDatabase getDatabase() {
        return writableDB;
    }

    /**
     * 关闭SqliteDatabase
     */
    public synchronized void closeDatabase() {
        if (writableDB != null && writableDB.isOpen()) {
            writableDB.close();
        }
    }

    /**
     * 分批插入多媒体数据
     */
    private void insertByGroup(int usbFlag, ContentValues contentValues) {
        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                insertValuesToDB(valuesList1, contentValues);
                break;
            case MultimediaConstants.FLAG_USB2:
                insertValuesToDB(valuesList2, contentValues);
                break;
            default:
                break;
        }
    }

    /**
     * 将集合数据插入到数据库中
     */
    private void insertValuesToDB(List<ContentValues> list, ContentValues contentValues) {
        if (list != null && list.size() >= capacity) {// 达到批量插入数量，开始批量插入数据库
            SQLiteDatabase database = getDatabase();
            // 开启事务
            database.beginTransaction();
            try {
                for (ContentValues values : list) {
                    // 每条数据在插入数据库之前，先判断对应的U盘是否依然挂载
                    switch (values.getAsInteger(DBConfiguration.USB_FLAG)) {
                        case MultimediaConstants.FLAG_USB1:
                            if (!UsbStateManager.getInstance().isUsb1Mounted()) {//
                                // 如果U盘拔出了，则中断插入，并且立马返回，事务回滚
                                // 清空暂存数据
                                list.clear();
                                return;
                            }
                            break;
                        case MultimediaConstants.FLAG_USB2:
                            if (!UsbStateManager.getInstance().isUsb2Mounted()) {//
                                // 如果U盘拔出了，则中断插入，并且立马返回，事务回滚
                                // 清空暂存数据
                                list.clear();
                                return;
                            }
                            break;
                        default:
                            break;
                    }
                    // 插入数据库
                    switch (values.getAsInteger(DBConfiguration.FILE_FLAG)) {
                        case DBConfiguration.FLAG_PHOTO:
                            database.insert(DBConfiguration.PhotoConfiguration.TABLE_NAME, null,
                                    values);
                            break;
                        case DBConfiguration.FLAG_MUSIC:
                            database.insert(DBConfiguration.MusicConfiguration.TABLE_NAME, null,
                                    values);
                            break;
                        case DBConfiguration.FLAG_LYRIC:
                            database.insert(DBConfiguration.LyricConfiguration.TABLE_NAME, null,
                                    values);
                            break;
                        case DBConfiguration.FLAG_VIDEO:
                            database.insert(DBConfiguration.VideoConfiguration.TABLE_NAME, null,
                                    values);
                            break;
                        default:
                            break;
                    }
                }
                // 该批数据插入成功后，清空List中暂存的数据
                list.clear();
                // 设置事务标志为成功，当结束事务时就会提交事务，否则事务回滚
                database.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
                Logger.logE("MediaDBManager---------------批量插入多媒体数据错误！");
            } finally {
                // 结束事务
                database.endTransaction();
            }
        } else {// 未达到批量插入数量，暂存在List集合中
            list.add(contentValues);
        }
    }

    /**
     * 扫描结束后批量插入剩余的多媒体数据
     */
    public void insertLastGroupData(int usbFlag) {
        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                insertLastValuesToDB(valuesList1);
                break;
            case MultimediaConstants.FLAG_USB2:
                insertLastValuesToDB(valuesList2);
                break;
            default:
                break;
        }
    }

    /**
     * 插入剩余的数据到数据库
     */
    private void insertLastValuesToDB(List<ContentValues> list) {
        SQLiteDatabase database = getDatabase();
        // 开始事务
        database.beginTransaction();
        try {
            for (ContentValues values : list) {
                // 如果U盘拔出了，停止插入
                switch (values.getAsInteger(DBConfiguration.USB_FLAG)) {
                    case MultimediaConstants.FLAG_USB1:
                        if (!UsbStateManager.getInstance().isUsb1Mounted()) {//
                            // 如果U盘拔出了，则中断插入，并且立马返回，事务回滚
                            // 清空暂存数据
                            list.clear();
                            return;
                        }
                        break;
                    case MultimediaConstants.FLAG_USB2:
                        if (!UsbStateManager.getInstance().isUsb2Mounted()) {//
                            // 如果U盘拔出了，则中断插入，并且立马返回，事务回滚
                            // 清空暂存数据
                            list.clear();
                            return;
                        }
                        break;
                    default:
                        break;
                }
                // 插入数据库
                switch (values.getAsInteger(DBConfiguration.FILE_FLAG)) {
                    case DBConfiguration.FLAG_PHOTO:
                        database.insert(DBConfiguration.PhotoConfiguration.TABLE_NAME, null,
                                values);
                        break;
                    case DBConfiguration.FLAG_MUSIC:
                        database.insert(DBConfiguration.MusicConfiguration.TABLE_NAME, null,
                                values);
                        break;
                    case DBConfiguration.FLAG_LYRIC:
                        database.insert(DBConfiguration.LyricConfiguration.TABLE_NAME, null,
                                values);
                        break;
                    case DBConfiguration.FLAG_VIDEO:
                        database.insert(DBConfiguration.VideoConfiguration.TABLE_NAME, null,
                                values);
                        break;
                    default:
                        break;
                }
            }
            // 该批数据插入成功后，清空List中暂存的数据
            list.clear();
            // 设置事务标志为成功，当结束事务时就会提交事务，否则事务回滚
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logE("MediaDBManager---------------插入剩余多媒体数据错误！");
        } finally {
            // 结束事务
            database.endTransaction();
        }
    }

    /**
     * 插入图片文件信息
     */
    public void insertPhoto(int usbFlag, String photoUri, String folderUri) {
        ContentValues values = new ContentValues();
        values.put(DBConfiguration.USB_FLAG, usbFlag);
        values.put(DBConfiguration.FILE_FLAG, DBConfiguration.FLAG_PHOTO);
        values.put(DBConfiguration.PhotoConfiguration.PHOTO_URI, photoUri);
        values.put(DBConfiguration.PhotoConfiguration.PHOTO_FOLDER_URI, folderUri);
        insertByGroup(usbFlag, values);
    }

    /**
     * 查询所有图片
     */
    public Cursor queryAllPhoto() {
        return writableDB.query(DBConfiguration.PhotoConfiguration.TABLE_NAME, new
                String[]{DBConfiguration.PhotoConfiguration._ID, DBConfiguration
                .PhotoConfiguration.PHOTO_URI}, null, null, null, null, DBConfiguration
                .PhotoConfiguration.DEFAULT_SORT_ORDER);
    }

    /**
     * 查询指定U盘的所有图片
     */
    public Cursor queryAllPhoto(int usbFlag) {
        return writableDB.query(DBConfiguration.PhotoConfiguration.TABLE_NAME, new
                String[]{DBConfiguration.PhotoConfiguration._ID, DBConfiguration
                .PhotoConfiguration.PHOTO_URI}, DBConfiguration.USB_FLAG + "=?", new
                String[]{String.valueOf(usbFlag)}, null, null, DBConfiguration.PhotoConfiguration
                .DEFAULT_SORT_ORDER);
    }

    /**
     * 获取指定文件夹下的所有图片（包括子文件夹中的图片）
     */
    public Cursor queryPhotoIncludeFolder(String folderUri) {
        return writableDB.query(DBConfiguration.PhotoConfiguration.TABLE_NAME, new
                String[]{DBConfiguration.PhotoConfiguration._ID, DBConfiguration
                .PhotoConfiguration.PHOTO_URI}, DBConfiguration.PhotoConfiguration.PHOTO_URI + " " +
                "" + "like ?", new String[]{folderUri + File.separator + "%"}, null, null,
                DBConfiguration.PhotoConfiguration.DEFAULT_SORT_ORDER);
    }

    /**
     * 获取指定文件夹下的所有图片（不包括子文件夹中的图片）
     */
    public Cursor queryPhotoExcludeFolder(String folderUri) {
        String sql = "select " + DBConfiguration.PhotoConfiguration._ID + ", " + DBConfiguration
                .PhotoConfiguration.PHOTO_URI + " from " + DBConfiguration.PhotoConfiguration
                .TABLE_NAME + " where " + DBConfiguration.PhotoConfiguration.PHOTO_URI + " like " +
                "'" + folderUri + File.separator + "%'" + " and " + DBConfiguration
                .PhotoConfiguration.PHOTO_URI + " not in(select " + DBConfiguration
                .PhotoConfiguration.PHOTO_URI + " " + "from " + DBConfiguration
                .PhotoConfiguration.TABLE_NAME + " where " + DBConfiguration.PhotoConfiguration
                .PHOTO_URI + " like '" + folderUri + File.separator + "%" + File.separator + "%')" +
                "" + " order by " + DBConfiguration.PhotoConfiguration.DEFAULT_SORT_ORDER;
        return writableDB.rawQuery(sql, null);
    }

    /**
     * 获取指定文件夹下的直属图片文件及包含图片文件的直属子文件夹（该子文件夹只添加一次）
     */
    public Cursor queryPhotoDirectUnder(String folderUri) {
        String sql = "select " + DBConfiguration.PhotoConfiguration.PHOTO_URI + " from " +
                DBConfiguration.PhotoConfiguration.TABLE_NAME + " where " + DBConfiguration
                .PhotoConfiguration.PHOTO_FOLDER_URI + " like '" + folderUri + File.separator +
                "%' group by " + DBConfiguration.PhotoConfiguration.PHOTO_FOLDER_URI + " union "
                + "select " + DBConfiguration.PhotoConfiguration.PHOTO_URI + " from " +
                DBConfiguration.PhotoConfiguration.TABLE_NAME + " where " + DBConfiguration
                .PhotoConfiguration.PHOTO_FOLDER_URI + " = '" + folderUri + "' order by " +
                DBConfiguration.PhotoConfiguration.DEFAULT_SORT_ORDER;
        return writableDB.rawQuery(sql, null);
    }

    /**
     * 删除指定U盘的图片信息
     */
    public void deletePhoto(int usbFlag) {
        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                writableDB.delete(DBConfiguration.PhotoConfiguration.TABLE_NAME, DBConfiguration
                        .PhotoConfiguration.PHOTO_URI + " like ?", new
                        String[]{MultimediaConstants.PATH_USB1 + File.separator + "%"});
                break;
            case MultimediaConstants.FLAG_USB2:
                writableDB.delete(DBConfiguration.PhotoConfiguration.TABLE_NAME, DBConfiguration
                        .PhotoConfiguration.PHOTO_URI + " like ?", new
                        String[]{MultimediaConstants.PATH_USB2 + File.separator + "%"});
                break;
            default:
                break;
        }
    }

    /**
     * 插入音乐文件信息
     */
    public void insertMusic(int usbFlag, String musicUri, String folderUri) {
        ContentValues values = new ContentValues();
        values.put(DBConfiguration.USB_FLAG, usbFlag);
        values.put(DBConfiguration.FILE_FLAG, DBConfiguration.FLAG_MUSIC);
        values.put(DBConfiguration.MusicConfiguration.MUSIC_URI, musicUri);
        values.put(DBConfiguration.MusicConfiguration.MUSIC_FOLDER_URI, folderUri);
        insertByGroup(usbFlag, values);
    }

    /**
     * 查询所有音乐
     */
    public Cursor queryAllMusic() {
        return writableDB.query(DBConfiguration.MusicConfiguration.TABLE_NAME, new
                String[]{DBConfiguration.MusicConfiguration._ID, DBConfiguration
                .MusicConfiguration.MUSIC_URI}, null, null, null, null, DBConfiguration
                .MusicConfiguration.DEFAULT_SORT_ORDER);
    }

    /**
     * 查询指定U盘的所有音乐
     */
    public Cursor queryAllMusic(int usbFlag) {
        return writableDB.query(DBConfiguration.MusicConfiguration.TABLE_NAME, new
                String[]{DBConfiguration.MusicConfiguration._ID, DBConfiguration
                .MusicConfiguration.MUSIC_URI}, DBConfiguration.USB_FLAG + "=?", new
                String[]{String.valueOf(usbFlag)}, null, null, DBConfiguration.MusicConfiguration
                .DEFAULT_SORT_ORDER);
    }

    /**
     * 获取指定文件夹下的所有音乐（包括子文件夹中的音乐）
     */
    public Cursor queryMusicIncludeFolder(String folderUri) {
        return writableDB.query(DBConfiguration.MusicConfiguration.TABLE_NAME, new
                String[]{DBConfiguration.MusicConfiguration._ID, DBConfiguration
                .MusicConfiguration.MUSIC_URI}, DBConfiguration.MusicConfiguration.MUSIC_URI + " " +
                "" + "like ?", new String[]{folderUri + File.separator + "%"}, null, null,
                DBConfiguration.MusicConfiguration.DEFAULT_SORT_ORDER);
    }

    /**
     * 获取指定文件夹下的所有音乐（不包含含有音乐文件的文件夹）
     */
    public Cursor queryMusicExcludeFolder(String folderUri) {
        String sql = "select " + DBConfiguration.MusicConfiguration._ID + ", " + DBConfiguration
                .MusicConfiguration.MUSIC_URI + " from " + DBConfiguration.MusicConfiguration
                .TABLE_NAME + " where " + DBConfiguration.MusicConfiguration.MUSIC_URI + " like " +
                "'" + folderUri + File.separator + "%'" + " and " + DBConfiguration
                .MusicConfiguration.MUSIC_URI + " not in(select " + DBConfiguration
                .MusicConfiguration.MUSIC_URI + " " + "from " + DBConfiguration
                .MusicConfiguration.TABLE_NAME + " where " + DBConfiguration.MusicConfiguration
                .MUSIC_URI + " like '" + folderUri + File.separator + "%" + File.separator + "%')" +
                "" + " order by " + DBConfiguration.MusicConfiguration.DEFAULT_SORT_ORDER;
        return writableDB.rawQuery(sql, null);
    }

    /**
     * 获取指定文件夹下的直属音乐及包含音乐文件的直属子文件夹（该文件夹只添加一次）
     */
    public Cursor queryMusicDirectUnder(String folderUri) {
        String sql = "select " + DBConfiguration.MusicConfiguration.MUSIC_URI + " from " +
                DBConfiguration.MusicConfiguration.TABLE_NAME + " where " + DBConfiguration
                .MusicConfiguration.MUSIC_FOLDER_URI + " like '" + folderUri + File.separator +
                "%' group by " + DBConfiguration.MusicConfiguration.MUSIC_FOLDER_URI + " union "
                + "select " + DBConfiguration.MusicConfiguration.MUSIC_URI + " from " +
                DBConfiguration.MusicConfiguration.TABLE_NAME + " where " + DBConfiguration
                .MusicConfiguration.MUSIC_FOLDER_URI + " = '" + folderUri + "' order by " +
                DBConfiguration.MusicConfiguration.DEFAULT_SORT_ORDER;
        return writableDB.rawQuery(sql, null);
    }

    /**
     * 删除指定U盘的歌曲信息
     */
    public void deleteMusic(int usbFlag) {
        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                writableDB.delete(DBConfiguration.MusicConfiguration.TABLE_NAME, DBConfiguration
                        .MusicConfiguration.MUSIC_URI + " like ?", new
                        String[]{MultimediaConstants.PATH_USB1 + File.separator + "%"});
                break;
            case MultimediaConstants.FLAG_USB2:
                writableDB.delete(DBConfiguration.MusicConfiguration.TABLE_NAME, DBConfiguration
                        .MusicConfiguration.MUSIC_URI + " like ?", new
                        String[]{MultimediaConstants.PATH_USB2 + File.separator + "%"});
                break;
            default:
                break;
        }
    }

    /**
     * 插入歌词文件信息
     */
    public void insertLyric(int usbFlag, String lyricUri, String lyricName) {
        ContentValues values = new ContentValues();
        values.put(DBConfiguration.USB_FLAG, usbFlag);
        values.put(DBConfiguration.FILE_FLAG, DBConfiguration.FLAG_LYRIC);
        values.put(DBConfiguration.LyricConfiguration.LYRIC_URI, lyricUri);
        values.put(DBConfiguration.LyricConfiguration.LYRIC_NAME, lyricName);
        insertByGroup(usbFlag, values);
    }

    /**
     * 获取与音乐匹配（名字相同）的歌词Url
     */
    public Cursor queryLyric(String musicUri) {
        int usbFlag = 0;
        if (musicUri.startsWith(MultimediaConstants.PATH_USB1)) {
            usbFlag = MultimediaConstants.FLAG_USB1;
        } else if (musicUri.startsWith(MultimediaConstants.PATH_USB2)) {
            usbFlag = MultimediaConstants.FLAG_USB2;
        }
        // 获取歌曲的名字（不包含后缀）
        String musicTitle = FileUriUtil.getFileTitle(musicUri);
        return writableDB.query(DBConfiguration.LyricConfiguration.TABLE_NAME, new
                String[]{DBConfiguration.LyricConfiguration.LYRIC_URI}, DBConfiguration.USB_FLAG
                + "=? and " + DBConfiguration.LyricConfiguration.LYRIC_NAME + "=?", new
                String[]{String.valueOf(usbFlag), musicTitle}, null, null, null);
    }

    /**
     * 删除指定U盘的歌词文件信息
     */
    public void deleteLyric(int usbFlag) {
        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                writableDB.delete(DBConfiguration.LyricConfiguration.TABLE_NAME, DBConfiguration
                        .LyricConfiguration.LYRIC_URI + " like ?", new
                        String[]{MultimediaConstants.PATH_USB1 + File.separator + "%"});
                break;
            case MultimediaConstants.FLAG_USB2:
                writableDB.delete(DBConfiguration.LyricConfiguration.TABLE_NAME, DBConfiguration
                        .LyricConfiguration.LYRIC_URI + " like ?", new
                        String[]{MultimediaConstants.PATH_USB2 + File.separator + "%"});
                break;
            default:
                break;
        }
    }

    /**
     * 插入视频文件信息
     */
    public void insertVideo(int usbFlag, String videoUri, String folderUri) {
        ContentValues values = new ContentValues();
        values.put(DBConfiguration.USB_FLAG, usbFlag);
        values.put(DBConfiguration.FILE_FLAG, DBConfiguration.FLAG_VIDEO);
        values.put(DBConfiguration.VideoConfiguration.VIDEO_URI, videoUri);
        values.put(DBConfiguration.VideoConfiguration.VIDEO_FOLDER_URI, folderUri);
        insertByGroup(usbFlag, values);
    }

    /**
     * 查询所有视频
     */
    public Cursor queryAllVideo() {
        return writableDB.query(DBConfiguration.VideoConfiguration.TABLE_NAME, new
                String[]{DBConfiguration.VideoConfiguration._ID, DBConfiguration
                .VideoConfiguration.VIDEO_URI}, null, null, null, null, DBConfiguration
                .VideoConfiguration.DEFAULT_SORT_ORDER);
    }

    /**
     * 查询指定U盘的所有视频
     */
    public Cursor queryAllVideo(int usbFlag) {
        return writableDB.query(DBConfiguration.VideoConfiguration.TABLE_NAME, new
                String[]{DBConfiguration.VideoConfiguration._ID, DBConfiguration
                .VideoConfiguration.VIDEO_URI}, DBConfiguration.USB_FLAG + "=?", new
                String[]{String.valueOf(usbFlag)}, null, null, DBConfiguration.VideoConfiguration
                .DEFAULT_SORT_ORDER);
    }

    /**
     * 获取指定文件夹下的所有视频（包括子文件夹中的视频）
     */
    public Cursor queryVideoIncludeFolder(String folderUri) {
        return writableDB.query(DBConfiguration.VideoConfiguration.TABLE_NAME, new
                String[]{DBConfiguration.VideoConfiguration._ID, DBConfiguration
                .VideoConfiguration.VIDEO_URI}, DBConfiguration.VideoConfiguration.VIDEO_URI + " " +
                "" + "like ?", new String[]{folderUri + File.separator + "%"}, null, null,
                DBConfiguration.VideoConfiguration.DEFAULT_SORT_ORDER);
    }

    /**
     * 获取指定文件夹下的所有视频（不包括子文件夹中的视频）
     */
    public Cursor queryVideoExcludeFolder(String folderUri) {
        String sql = "select " + DBConfiguration.VideoConfiguration._ID + ", " + DBConfiguration
                .VideoConfiguration.VIDEO_URI + " from " + DBConfiguration.VideoConfiguration
                .TABLE_NAME + " where " + DBConfiguration.VideoConfiguration.VIDEO_URI + " like " +
                "'" + folderUri + File.separator + "%'" + " and " + DBConfiguration
                .VideoConfiguration.VIDEO_URI + " not in(select " + DBConfiguration
                .VideoConfiguration.VIDEO_URI + " from " + DBConfiguration.VideoConfiguration
                .TABLE_NAME + " where " + DBConfiguration.VideoConfiguration.VIDEO_URI + " like " +
                "'" + folderUri + File.separator + "%" + File.separator + "%')" + " order by " +
                DBConfiguration.VideoConfiguration.DEFAULT_SORT_ORDER;
        return writableDB.rawQuery(sql, null);
    }

    /**
     * 获取指定文件夹下的直属视频文件及包含视频文件的直属文件夹（该文件夹只添加一次）
     */
    public Cursor queryVideoDirectUnder(String folderUri) {
        String sql = "select " + DBConfiguration.VideoConfiguration.VIDEO_URI + " from " +
                DBConfiguration.VideoConfiguration.TABLE_NAME + " where " + DBConfiguration
                .VideoConfiguration.VIDEO_FOLDER_URI + " like '" + folderUri + File.separator +
                "%' group by " + DBConfiguration.VideoConfiguration.VIDEO_FOLDER_URI + " union " +
                "select " + DBConfiguration.VideoConfiguration.VIDEO_URI + " from " +
                DBConfiguration.VideoConfiguration.TABLE_NAME + " where " + DBConfiguration
                .VideoConfiguration.VIDEO_FOLDER_URI + " = '" + folderUri + "' order by " +
                DBConfiguration.VideoConfiguration.DEFAULT_SORT_ORDER;
        return writableDB.rawQuery(sql, null);
    }

    /**
     * 删除指定U盘的视频文件信息
     */
    public void deleteVideo(int usbFlag) {
        switch (usbFlag) {
            case MultimediaConstants.FLAG_USB1:
                writableDB.delete(DBConfiguration.VideoConfiguration.TABLE_NAME, DBConfiguration
                        .VideoConfiguration.VIDEO_URI + " like ?", new
                        String[]{MultimediaConstants.PATH_USB1 + File.separator + "%"});
                break;
            case MultimediaConstants.FLAG_USB2:
                writableDB.delete(DBConfiguration.VideoConfiguration.TABLE_NAME, DBConfiguration
                        .VideoConfiguration.VIDEO_URI + " like ?", new
                        String[]{MultimediaConstants.PATH_USB2 + File.separator + "%"});
                break;
            default:
                break;
        }
    }
}
