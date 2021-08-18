import { HttpClient, HttpEvent } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FileService {

  private server = 'http://localhost:8080/file';
  constructor(private httpClient:HttpClient) { }

  //define upload file

  upload(formData:FormData):Observable<HttpEvent<string[]>>
  {
      return this.httpClient.post<string[]>(`${this.server}/upload`,formData,{
        reportProgress:true,
        observe:'events'
      });
  }
  //define function to download file
  download(filename:string):Observable<HttpEvent<Blob>>
  {
      return this.httpClient.get(`${this.server}/download/${filename}`,{
        reportProgress:true,
        observe:'events',
        responseType:'blob'
      });
  }
}
