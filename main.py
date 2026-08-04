import sqlite3
import tkinter as tk
from tkinter import ttk
from tkinter import messagebox
import pandas as pd
from datetime import datetime

# ==========================================
# 1. إعداد قاعدة البيانات
# ==========================================
def setup_database():
    conn = sqlite3.connect('ice_factory.db')
    cursor = conn.cursor()
    
    # جدول الإنتاج والمبيعات اليومية (يشمل الهالك)
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS daily_records (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            date TEXT,
            produced INTEGER,
            sold INTEGER,
            damaged INTEGER,
            price_per_block REAL,
            total_income REAL
        )
    ''')
    
    # جدول الخزينة (لتوزيع القروش على الحسابات)
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS treasury (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            date TEXT,
            cash REAL,
            bankak REAL,
            ocash REAL,
            fawry REAL
        )
    ''')
    
    # جدول المنصرفات (صيانة، رواتب، طاقة/وقود)
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS expenses (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            date TEXT,
            category TEXT,
            amount REAL,
            notes TEXT
        )
    ''')

    # جدول ديون العملاء
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS debts (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            client_name TEXT,
            phone TEXT,
            amount REAL,
            date TEXT
        )
    ''')
    
    conn.commit()
    conn.close()

# ==========================================
# 2. واجهة المستخدم الأساسية (GUI)
# ==========================================
class IceFactoryApp:
    def __init__(self, root):
        self.root = root
        self.root.title("برنامج إدارة مصنع الثلج")
        self.root.geometry("800x600")
        
        # إنشاء نظام التبويبات (Tabs) لتنظيم الشاشات
        self.notebook = ttk.Notebook(root)
        self.notebook.pack(fill='both', expand=True)
        
        # تبويب اليومية (الإنتاج والمبيعات)
        self.tab_daily = ttk.Frame(self.notebook)
        self.notebook.add(self.tab_daily, text="الإنتاج والمبيعات اليومية")
        
        # تبويب الخزينة (بنكك، أوكاش، كاش)
        self.tab_treasury = ttk.Frame(self.notebook)
        self.notebook.add(self.tab_treasury, text="توزيع الخزينة")
        
        # تبويب المنصرفات (صيانة وعمال)
        self.tab_expenses = ttk.Frame(self.notebook)
        self.notebook.add(self.tab_expenses, text="المنصرفات والصيانة")
        
        # تبويب الديون
        self.tab_debts = ttk.Frame(self.notebook)
        self.notebook.add(self.tab_debts, text="ديون العملاء")
        
        # تبويب تصدير الإكسل
        self.tab_export = ttk.Frame(self.notebook)
        self.notebook.add(self.tab_export, text="تصدير إلى Excel")

        self.setup_daily_tab()
        self.setup_export_tab()

    def setup_daily_tab(self):
        # عناصر واجهة إدخال الإنتاج والمبيعات
        ttk.Label(self.tab_daily, text="تاريخ اليوم:").grid(row=0, column=0, padx=10, pady=10)
        self.entry_date = ttk.Entry(self.tab_daily)
        self.entry_date.insert(0, datetime.today().strftime('%Y-%m-%d'))
        self.entry_date.grid(row=0, column=1)

        ttk.Label(self.tab_daily, text="عدد الألواح المنتجة:").grid(row=1, column=0, padx=10, pady=10)
        self.entry_produced = ttk.Entry(self.tab_daily)
        self.entry_produced.grid(row=1, column=1)

        ttk.Label(self.tab_daily, text="عدد الألواح المباعة:").grid(row=2, column=0, padx=10, pady=10)
        self.entry_sold = ttk.Entry(self.tab_daily)
        self.entry_sold.grid(row=2, column=1)

        ttk.Label(self.tab_daily, text="عدد الألواح التالفة:").grid(row=3, column=0, padx=10, pady=10)
        self.entry_damaged = ttk.Entry(self.tab_daily)
        self.entry_damaged.grid(row=3, column=1)

        ttk.Label(self.tab_daily, text="سعر بيع اللوح:").grid(row=4, column=0, padx=10, pady=10)
        self.entry_price = ttk.Entry(self.tab_daily)
        self.entry_price.grid(row=4, column=1)

        ttk.Button(self.tab_daily, text="حفظ بيانات اليوم", command=self.save_daily_data).grid(row=5, column=0, columnspan=2, pady=20)

    def save_daily_data(self):
        # هنا سنكتب كود حفظ البيانات في قاعدة البيانات لاحقاً
        messagebox.showinfo("نجاح", "تم حفظ بيانات الإنتاج والمبيعات بنجاح!")

    def setup_export_tab(self):
        ttk.Label(self.tab_export, text="تصدير جميع البيانات إلى ملف Excel بضغطة زر").pack(pady=20)
        ttk.Button(self.tab_export, text="استخراج ملف Excel", command=self.export_to_excel).pack(pady=10)

    def export_to_excel(self):
        # كود استخراج البيانات إلى إكسل باستخدام Pandas
        try:
            conn = sqlite3.connect('ice_factory.db')
            
            # قراءة الجداول
            df_daily = pd.read_sql_query("SELECT * FROM daily_records", conn)
            df_treasury = pd.read_sql_query("SELECT * FROM treasury", conn)
            df_expenses = pd.read_sql_query("SELECT * FROM expenses", conn)
            df_debts = pd.read_sql_query("SELECT * FROM debts", conn)
            
            # حفظ في ملف إكسل بصفحات متعددة
            filename = f"تقرير_المصنع_{datetime.today().strftime('%Y_%m_%d')}.xlsx"
            with pd.ExcelWriter(filename) as writer:
                df_daily.to_excel(writer, sheet_name='اليومية', index=False)
                df_treasury.to_excel(writer, sheet_name='الخزينة', index=False)
                df_expenses.to_excel(writer, sheet_name='المنصرفات', index=False)
                df_debts.to_excel(writer, sheet_name='الديون', index=False)
            
            conn.close()
            messagebox.showinfo("نجاح", f"تم استخراج الملف بنجاح باسم:\n{filename}")
        except Exception as e:
            messagebox.showerror("خطأ", f"حدث خطأ أثناء التصدير:\n{str(e)}")

# ==========================================
# 3. تشغيل البرنامج
# ==========================================
if __name__ == "__main__":
    setup_database() # إنشاء الجداول إذا لم تكن موجودة
    root = tk.Tk()
    app = IceFactoryApp(root)
    root.mainloop()
