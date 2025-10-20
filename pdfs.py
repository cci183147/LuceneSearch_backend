import os
import requests

input_dir = r"G:\luence\LuceneSearch\src\main\resources\pdf_files"
output_dir = r"G:\测试\backend\LuceneSearch_backend\src\main\resources\pdf_files"
url = "http://localhost:8070/api/processFulltextDocument"

# 确保输出目录存在
os.makedirs(output_dir, exist_ok=True)

for pdf_file in os.listdir(input_dir):
    if pdf_file.endswith(".pdf"):
        pdf_path = os.path.join(input_dir, pdf_file)
        print(f"正在处理: {pdf_file}")

        with open(pdf_path, 'rb') as f:
            files = {'input': f}
            try:
                response = requests.post(url, files=files)
                response.raise_for_status()  # 检查请求是否成功

                output_filename = pdf_file.replace(".pdf", ".xml")
                output_path = os.path.join(output_dir, output_filename)

                with open(output_path, 'w', encoding='utf-8') as output_file:
                    output_file.write(response.text)
                print(f"成功处理: {pdf_file} -> {output_filename}")

            except requests.exceptions.RequestException as e:
                print(f"处理 {pdf_file} 时出错: {e}")
            except Exception as e:
                print(f"处理 {pdf_file} 时发生未知错误: {e}")