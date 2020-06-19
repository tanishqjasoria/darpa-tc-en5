 
from pydrive.auth import GoogleAuth
from pydrive.drive import GoogleDrive
#from google.colab import auth
from oauth2client.client import GoogleCredentials
import os
#from memory_profiler import profile
from pympler import muppy, summary
import pandas as pd

#auth.authenticate_user()
gauth = GoogleAuth()
gauth.credentials = GoogleCredentials.get_application_default()
drive = GoogleDrive(gauth)


def download_folder(id):
  file_list = drive.ListFile({'q': "'{}' in parents and trashed=false".format(id)}).GetList()
  for file_folder in file_list:
    get_mem()
    if "folder" in file_folder['mimeType']:
      try:
        os.mkdir(file_folder['title'])
        os.chdir(file_folder['title'])
        print("Making folder: ", end=" ")
        print(file_folder['title'])
        download_folder(file_folder['id'])
        os.chdir('..')
      except Exception as e:
        #track = traceback.format_exc()
        #print(track)
        print(e)
        #os.chdir('..')
    else:
      try:
        print("Downloading file: ", end=" ")
        print(file_folder['title'])
        file_folder.GetContentFile(file_folder['title'])
        file_folder.__dict__['attr']['content'].__del__()
      except Exception as e:
        #track = traceback.format_exc()
        #print(track)
        print(e)

def get_mem():
  all_objects = muppy.get_objects()
  sum1 = summary.summarize(all_objects)
  # Prints out a summary of the large objects
  summary.print_(sum1)
  # Get references to certain types of objects such as dataframe
  dataframes = [ao for ao in all_objects if isinstance(ao, pd.DataFrame)]
  for d in dataframes:
    print(d.columns.values)
    print(len(d))

download_folder("1okt4AYElyBohW4XiOBqmsvjwXsnUjLVf")
