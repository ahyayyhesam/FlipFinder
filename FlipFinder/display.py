import tkinter as tk
from tkinter import scrolledtext
import os

def update_text():
    try:
        with open("bazaar_flips.txt", "r") as file:  # <-- CHANGE THIS PATH
            content = file.read()
        text_area.configure(state='normal')  # Temporarily enable editing
        text_area.delete('1.0', tk.END)
        text_area.insert(tk.END, content)
        text_area.configure(state='disabled')  # Disable editing again
    except FileNotFoundError:
        text_area.configure(state='normal')
        text_area.delete('1.0', tk.END)
        text_area.insert(tk.END, "File not found!")
        text_area.configure(state='disabled')
    root.after(1000, update_text)  # Update every 1 second

root = tk.Tk()
root.title("Live Text Overlay")
root.attributes('-alpha', 0.7)
root.attributes('-topmost', True)
root.geometry("600x400+100+100")  # Larger default size

# Use a monospace font and proper text wrapping
font = ('Consolas', 8)  # Monospace font for consistent alignment
bg_color = '#202020'  # Dark gray
fg_color = '#FFFFFF'  # White

text_area = scrolledtext.ScrolledText(
    root,
    wrap=tk.WORD,  # Proper word wrapping
    font=font,
    bg=bg_color,
    fg=fg_color,
    padx=10,  # Padding for cleaner look
    pady=10,
    state='disabled'  # Prevent accidental editing
)
text_area.pack(fill=tk.BOTH, expand=True)

update_text()
root.mainloop()