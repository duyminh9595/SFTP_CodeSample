import { HttpErrorResponse, HttpEvent, HttpEventType } from '@angular/common/http';
import { Component } from '@angular/core';
import {saveAs} from 'file-saver';
import { FileService } from './file.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  filenames:string[]=[];
  fileStatus={status:'',requestType:'',percent:0};
  constructor(private fileService: FileService) { }

  onUploadFiles(files: File[]): void {
    const formData = new FormData();
    for (const file of files) {
      formData.append('files', file, file.name);
    }
    this.fileService.upload(formData).subscribe
      (
        event => {
          console.log(event);
          this.reportProgress(event);
        },
        (error: HttpErrorResponse) => {
          console.log(error);
        }
      );
  }
  reportProgress(httpEvent: HttpEvent<string[] | Blob>): void {

    switch (httpEvent.type) {
      case HttpEventType.UploadProgress:
        this.updateStatus(httpEvent.loaded, httpEvent.total!, 'Uploading');
        break;
      case HttpEventType.DownloadProgress:
        this.updateStatus(httpEvent.loaded, httpEvent.total!, 'Downloading');
        break;
      case HttpEventType.ResponseHeader:
        console.log('Header return', httpEvent);
        break;
      case HttpEventType.Response:
        if (httpEvent.body instanceof Array) {
          for(const filename of httpEvent.body)
          {
            this.filenames.unshift(filename);
          }
        }
        else {
            saveAs(new File([httpEvent.body!],httpEvent.headers.get('File-Name')!,
              {type:`${httpEvent.headers.get('Content-Type')};charset=utf-8`}));
              // saveAs(new Blob([httpEvent.body!],{type:`${httpEvent.headers.get('Content-Type')};charset=utf-8`}),
              //     httpEvent.headers.get('File-Name'));
        }
        this.fileStatus.status='done';
        break;
      default:
        console.log(httpEvent);
        break;
    }

  }
  private updateStatus(loaded: number, total: number , requestType: string) {
    this.fileStatus.status='progress';
    this.fileStatus.requestType=requestType;
    this.fileStatus.percent=Math.round(100 * loaded/total);
  }
  onDownloaddFiles(filename: string): void {

    this.fileService.download(filename).subscribe
      (
        event => {
          console.log(event);
          this.reportProgress(event);
        },
        (error: HttpErrorResponse) => {
          console.log(error);
        }
      );
  }
}
