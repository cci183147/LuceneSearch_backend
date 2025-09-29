import os
import requests

input_dir = "/home/cci/pdfs/oriPDFs"
output_dir = "/home/output"
url = "http://localhost:8070/api/processFulltextDocument"

for pdf_file in os.listdir(input_dir):
    if pdf_file.endswith(".pdf"):
        with open(os.path.join(input_dir, pdf_file), 'rb') as f:
            files = {'input': f}
            response = requests.post(url, files=files)
            with open(os.path.join(output_dir, pdf_file.replace(".pdf", ".xml")), 'w') as output_file:
                output_file.write(response.text)

