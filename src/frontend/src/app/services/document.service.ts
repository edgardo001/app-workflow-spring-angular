import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UploadedDocument {
  id: string;
  name: string;
  size: string;
  type: string;
  url: string;
}

@Injectable({ providedIn: 'root' })
export class DocumentService {
  constructor(private http: HttpClient) {}

  upload(file: File): Observable<UploadedDocument> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<UploadedDocument>('/api/documents/upload', formData);
  }

  uploadMultiple(files: File[]): Observable<UploadedDocument[]> {
    const formData = new FormData();
    files.forEach(f => formData.append('files', f));
    return this.http.post<UploadedDocument[]>('/api/documents/upload-multiple', formData);
  }

  getDownloadUrl(documentId: string): string {
    return `/api/documents/${documentId}/download`;
  }
}
