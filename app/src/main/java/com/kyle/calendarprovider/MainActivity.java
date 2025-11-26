/**
 * 作者：地狱丧钟/叁大（GitHub：@Hell Alarm）
 * 邮箱：sandatt517@outlook.com
 * 创建日期：2025-11-26
 * 版权声明：本代码基于 MIT 协议开源，可自由使用、修改、分发，需保留原作者声明
 * 项目地址：https://github.com/sanda-tt/android-Ebblinghaus.memory-notes
 * 功能：艾宾浩斯记忆助手
 */


package com.kyle.calendarprovider;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kyle.calendarprovider.calendar.CalendarEvent;
import com.kyle.calendarprovider.calendar.CalendarProviderManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_main_add)
    Button btnMainAdd;
    @BindView(R.id.btn_main_delete)
    Button btnMainDelete;
    @BindView(R.id.btn_main_update)
    Button btnMainUpdate;
    @BindView(R.id.btn_main_query)
    Button btnMainQuery;
    @BindView(R.id.tv_event)
    TextView tvEvent;
    @BindView(R.id.btn_edit)
    Button btnEdit;
    @BindView(R.id.btn_search)
    Button btnSearch;

    // 添加备注输入框的绑定
    @BindView(R.id.et_remark)
    EditText etRemark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.WRITE_CALENDAR,
                            Manifest.permission.READ_CALENDAR
                    }, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "权限被拒绝，部分功能无法使用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 检查日历权限
     */
    private boolean checkCalendarPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;
    }



    @OnClick({R.id.btn_main_add, R.id.btn_main_delete, R.id.btn_edit,
            R.id.btn_main_update, R.id.btn_main_query, R.id.btn_search})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_main_add:
                createVocabularyEvents();
                break;
            case R.id.btn_main_delete:
                deleteAllVocabularyEvents();
                break;
            case R.id.btn_main_update:
                updateFirstVocabularyEvent();
                break;
            case R.id.btn_main_query:
                queryAndDisplayEvents();
                break;
            case R.id.btn_edit:
                // 启动系统日历进行编辑事件，使用当前备注
                String currentRemark = etRemark.getText().toString().trim();
                if (currentRemark.isEmpty()) {
                    currentRemark = "背第一单元";
                }
                CalendarProviderManager.startCalendarForIntentToInsert(this, System.currentTimeMillis(),
                        System.currentTimeMillis() + 60000, "艾宾浩斯 - " + currentRemark,
                        currentRemark, "英语课本" + currentRemark, false);
                break;
            case R.id.btn_search:
                checkTodayVocabularyEvent();
                break;
            default:
                break;
        }
    }

    /**
     * 创建多个背单词事件
     */
    private void createVocabularyEvents() {
        String remark = etRemark.getText().toString().trim();
        if (remark.isEmpty()) {
            remark = "背第一单元";
            etRemark.setText(remark);
        }

        int[] days = {1, 2, 6, 14, 30}; // 1,2,6,14,30天后
        int successCount = 0;
        int totalCount = days.length;

        for (int day : days) {
            // 获取指定天数后的早上8点时间
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, day);
            calendar.set(Calendar.HOUR_OF_DAY, 8); // 早上8点
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            long startTime = calendar.getTimeInMillis();
            long endTime = startTime + 3600000; // 持续1小时

            // 创建背单词事件，使用用户自定义的备注
            CalendarEvent calendarEvent = new CalendarEvent(
                    "艾宾浩斯" + remark,  // 标题包含备注
                    remark,               // 描述使用备注
                    "艾宾浩斯" + remark,   // 地点也包含备注
                    startTime,
                    endTime,
                    0, null
            );

            // 添加事件
            int result = CalendarProviderManager.addCalendarEvent(this, calendarEvent);
            if (result == 0) {
                successCount++;
            } else if (result == -2) {
                Toast.makeText(this, "没有权限，无法创建事件", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 显示创建结果
        if (successCount == totalCount) {
            Toast.makeText(this, "成功创建 " + successCount + " 个艾宾浩斯事件", Toast.LENGTH_SHORT).show();
            queryAndDisplayEvents();
        } else {
            Toast.makeText(this, "创建完成: " + successCount + "/" + totalCount + " 个事件", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 删除所有背单词事件 - 修复版本
     */
    private void deleteAllVocabularyEvents() {
        // 检查权限
        if (!checkCalendarPermissions()) {
            Toast.makeText(this, "请先授予日历权限", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.WRITE_CALENDAR,
                            Manifest.permission.READ_CALENDAR
                    }, 1);
            return;
        }

        try {
            long calID = CalendarProviderManager.obtainCalendarAccountID(this);
            Log.d("CalendarDebug", "获取到的日历账户ID: " + calID);

            if (calID <= 0) {
                Toast.makeText(this, "无法获取日历账户", Toast.LENGTH_SHORT).show();
                return;
            }

            List<CalendarEvent> events = CalendarProviderManager.queryAccountEvent(this, calID);
            Log.d("CalendarDebug", "查询到的事件数量: " + (events != null ? events.size() : 0));

            if (events == null || events.isEmpty()) {
                Toast.makeText(this, "没有找到任何事件", Toast.LENGTH_SHORT).show();
                return;
            }

            int deleteCount = 0;
            int totalVocabEvents = 0;

            for (CalendarEvent event : events) {
                Log.d("CalendarDebug", "事件标题: " + event.getTitle() + ", ID: " + event.getId());

                // 放宽匹配条件
                if (event.getTitle() != null &&
                        (event.getTitle().contains("艾宾浩斯") ||
                                event.getTitle().contains("背单词") ||
                                (event.getDescription() != null && event.getDescription().contains("背")))) {

                    totalVocabEvents++;
                    int result = CalendarProviderManager.deleteCalendarEvent(this, event.getId());
                    Log.d("CalendarDebug", "删除事件结果: " + result + ", 事件ID: " + event.getId());

                    // 修改这里：根据实际的返回值判断成功
                    if (result >= 0) { // 通常>=0表示成功，具体看CalendarProviderManager的实现
                        deleteCount++;
                    } else if (result == -2) {
                        Toast.makeText(this, "删除事件时权限不足", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            if (totalVocabEvents == 0) {
                Toast.makeText(this, "没有找到艾宾浩斯相关事件", Toast.LENGTH_SHORT).show();
            } else if (deleteCount > 0) {
                Toast.makeText(this, "成功删除 " + deleteCount + " 个艾宾浩斯事件", Toast.LENGTH_SHORT).show();
                queryAndDisplayEvents();
            } else {
                Toast.makeText(this, "删除失败，请检查权限和事件是否存在", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("CalendarDebug", "删除事件异常", e);
            Toast.makeText(this, "删除事件时发生异常", Toast.LENGTH_SHORT).show();
        }
    }



    /**
     * 更新第一个艾宾浩斯事件
     */
    private void updateFirstVocabularyEvent() {
        long calID = CalendarProviderManager.obtainCalendarAccountID(this);
        List<CalendarEvent> events = CalendarProviderManager.queryAccountEvent(this, calID);

        if (null != events) {
            // 查找第一个背单词事件
            CalendarEvent vocabEvent = null;
            for (CalendarEvent event : events) {
                if (event.getTitle().contains("艾宾浩斯")) {
                    vocabEvent = event;
                    break;
                }
            }

            if (vocabEvent == null) {
                Toast.makeText(this, "没有找到艾宾浩斯事件", Toast.LENGTH_SHORT).show();
            } else {
                String newRemark = etRemark.getText().toString().trim();
                if (newRemark.isEmpty()) {
                    newRemark = "背第一单元";
                }

                int result = CalendarProviderManager.updateCalendarEventTitle(
                        this, vocabEvent.getId(), "艾宾浩斯 - " + newRemark);
                if (result == 1) {
                    Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
                    queryAndDisplayEvents();
                } else {
                    Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "查询失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 检查今天8点是否有背单词事件
     */
    private void checkTodayVocabularyEvent() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startTime = calendar.getTimeInMillis();
        long endTime = startTime + 3600000; // 1小时

        if (CalendarProviderManager.isEventAlreadyExist(this, startTime, endTime, "艾宾浩斯")) {
            Toast.makeText(this, "今天8点有艾宾浩斯事件", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "今天8点没有艾宾浩斯事件", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 查询并显示事件列表 - 修复版本
     */
    private void queryAndDisplayEvents() {
        try {
            long calID = CalendarProviderManager.obtainCalendarAccountID(this);
            Log.d("CalendarDebug", "查询使用的日历账户ID: " + calID);

            if (calID <= 0) {
                tvEvent.setText("无法获取日历账户");
                return;
            }

            List<CalendarEvent> events = CalendarProviderManager.queryAccountEvent(this, calID);
            StringBuilder stringBuilder = new StringBuilder();

            if (events != null && !events.isEmpty()) {
                stringBuilder.append("总共查询到 ").append(events.size()).append(" 个事件\n\n");

                int vocabEventCount = 0;
                for (CalendarEvent event : events) {
                    // 放宽匹配条件
                    boolean isVocabEvent = event.getTitle() != null &&
                            (event.getTitle().contains("艾宾浩斯") ||
                                    event.getTitle().contains("背单词"));

                    if (isVocabEvent) {
                        vocabEventCount++;
                        stringBuilder.append("📚 ").append(event.getTitle()).append("\n");
                        stringBuilder.append("描述: ").append(event.getDescription()).append("\n");
                        stringBuilder.append("地点: ").append(event.getEventLocation()).append("\n");
                        stringBuilder.append("开始时间: ").append(formatTime(event.getStart())).append("\n");
                        stringBuilder.append("结束时间: ").append(formatTime(event.getEnd())).append("\n");
                        stringBuilder.append("事件ID: ").append(event.getId()).append("\n");
                        stringBuilder.append("----------------------------\n\n");
                    }
                }

                if (vocabEventCount == 0) {
                    stringBuilder.append("没有找到艾宾浩斯事件\n\n");
                    // 显示前几个事件用于调试
                    stringBuilder.append("前3个事件标题:\n");
                    for (int i = 0; i < Math.min(3, events.size()); i++) {
                        stringBuilder.append(i + 1).append(". ").append(events.get(i).getTitle()).append("\n");
                    }
                } else {
                    stringBuilder.insert(0, "找到 " + vocabEventCount + " 个艾宾浩斯事件:\n\n");
                }
            } else {
                stringBuilder.append("没有查询到任何事件");
            }

            tvEvent.setText(stringBuilder.toString());

        } catch (Exception e) {
            Log.e("CalendarDebug", "查询事件异常", e);
            tvEvent.setText("查询事件时发生异常: " + e.getMessage());
        }
    }

    /**
     * 调试用：显示所有事件
     */
    private void showAllEventsForDebug(List<CalendarEvent> events) {
        StringBuilder debugInfo = new StringBuilder("所有事件列表:\n\n");
        for (int i = 0; i < events.size(); i++) {
            CalendarEvent event = events.get(i);
            debugInfo.append(i + 1).append(". 标题: ").append(event.getTitle())
                    .append("\n   ID: ").append(event.getId())
                    .append("\n   描述: ").append(event.getDescription())
                    .append("\n   开始时间: ").append(formatTime(event.getStart()))
                    .append("\n   ----------------------------\n");
        }
        tvEvent.setText(debugInfo.toString());
    }

    /**
     * 格式化时间显示
     */
    private String formatTime(long timeInMillis) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault());
            return sdf.format(timeInMillis);
        } catch (Exception e) {
            return "时间格式错误";
        }
    }
}