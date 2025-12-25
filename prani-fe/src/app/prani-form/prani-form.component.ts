import { Component, computed, inject, OnInit, Signal, signal, WritableSignal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { NzFormModule } from 'ng-zorro-antd/form';
import { NzImageModule } from 'ng-zorro-antd/image';
import { NzInputModule } from 'ng-zorro-antd/input';
import { UserService } from '../services/user-service';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzResultModule } from 'ng-zorro-antd/result';
import { NzNotificationService } from 'ng-zorro-antd/notification';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzUploadChangeParam, NzUploadFile, NzUploadModule } from 'ng-zorro-antd/upload';
import { TextContent as TextContent } from '../models/text-content';
import { JsonPipe } from '@angular/common';
import { PhotoInfo } from '../models/photo-info';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { catchError, map, Observable, of } from 'rxjs';

const getBase64 = (file: File): Promise<string | ArrayBuffer | null> =>
  new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = error => reject(error);
  });

const defaultPreviewUrl = 'data:image/png;base64,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';


@Component({
  selector: 'app-prani-form',
  imports: [FormsModule, NzFormModule, NzInputModule, NzInputModule, NzImageModule,
    NzAlertModule, NzButtonModule, NzResultModule, NzUploadModule, NzIconModule, JsonPipe],
  templateUrl: './prani-form.component.html',
  styleUrls: ['./prani-form.component.css']
})
export class PraniFormComponent implements OnInit {
  private userService = inject(UserService);
  private notification = inject(NzNotificationService);
  private message = inject(NzMessageService);
  private messageService = inject(NzMessageService);

  fotoFile = signal<File | null>(null);
  fotoInfo = signal<PhotoInfo | null>(null);
  previewUrl = signal<string | ArrayBuffer | null>(defaultPreviewUrl)
  textPrani = '';
  sentInfo = signal('');
  textContent = signal<TextContent | null>(null);
  fileList = signal<NzUploadFile[]>([]);

  ngOnInit(): void {
    console.log('PraniFormComponent :: ngOnInit');
    this.userService.getFotoPrani().subscribe({
      next: (nzUploadFile) => {
        this.fileList.set([nzUploadFile]);
        this.previewUrl.set(nzUploadFile.url || null);
      },
      error: (error) => console.error('Chyba:', error)
    });
    this.userService.getTextContent().subscribe({
      next: (textContent) => {
        console.log('textContent:', textContent);
        this.textContent.set(textContent);
        this.textPrani = textContent.content;
      },
      error: (error) => console.error('Chyba:', error)
    });
  }

  // onFileSelected(event: any) {
  //   const file = (event.target as HTMLInputElement).files?.[0] ?? null;
  //   this.fotoFile.set(file);

  //   if (file) {
  //     const reader = new FileReader();
  //     reader.onload = () => this.previewUrl.set(reader.result);
  //     reader.readAsDataURL(file);

  //     this.userService.updateFotoPrani(file).subscribe({
  //       next: (response) => console.log('Úspěch!', response),
  //       error: (error) => console.error('Chyba:', error)
  //     });
  //   } else {
  //     this.previewUrl.set(null);
  //   }
  // }

  submitForm(): void {
    console.log('PraniFormComponent :: submitForm :: textPrani:', this.textPrani);

    this.userService.updateTextPrani(this.textPrani).subscribe({
      next: (textPraniDto) => {
        console.log('PraniFormComponent :: submitForm sent successfully:', textPraniDto);
        this.textContent.set(textPraniDto);

        this.sentInfo.set('Formulář byl úspěšně odeslán.');
        this.notification.info('Success Title', 'This is a success message!', { nzDuration: 1000 });
        this.notification.error('Success Title', 'This is a success message!', { nzDuration: 2000 });
        this.notification.success('Success Title', 'This is a success message!', { nzDuration: 3000 });
        this.message.info('This is an informational message.');
        this.message.error('This is an informational message.');
        this.message.success('This is an informational message.');
      },
      error: (error) => {
        console.error('PraniFormComponent :: Error sending submitForm:', error);
        this.sentInfo.set('Chyba při odesílání formuláře.');
      }
    });
  }

  async handleChange(info: NzUploadChangeParam): Promise<void> {
    let fileList = [...info.fileList];

    // Limit - jeden soubor
    fileList = fileList.slice(-1);

    if (info.file.status !== 'uploading') {
      console.log('handleChange :: info:', info);
      //console.log(info.file, info.fileList);
    }
    if (info.file.status === 'done') {
      this.messageService.success(`${info.file.name} file uploaded successfully`);
      const imgUrl = await getBase64(info.file.originFileObj!);
      this.previewUrl.set(imgUrl);
      // info.file.response
    // } else if (info.file.status === 'removed') {
    //   this.userService.deleteFotoPrani().subscribe({
    //     next: () => {
    //       this.previewUrl.set(null);
    //       this.messageService.success('Fotografie byla úspěšně odstraněna');
    //     },
    //     error: () => {
    //       this.messageService.error('Odstranění fotografie se nezdařilo');
    //     }
    //   });
    } else if (info.file.status === 'error') {
      this.messageService.error(`${info.file.name} file upload failed.`);
    }

    this.fileList.set(fileList);
  }

  beforeUpload = (file: NzUploadFile): boolean => {
    const isImage = file.type?.startsWith('image/') ?? false;
    if (!isImage) {
      this.messageService.error('Vkládat se mohou jen obrázky');
    }
    return isImage;
  };


  removeFoto = (file: NzUploadFile): boolean | Observable<boolean> => {
    console.log('removeFoto :: file:', file);
    return this.userService.deleteFotoPrani().pipe(
      map(() => {
        const fileList = this.fileList().filter(f => f.uid !== file.uid)
        this.fileList.set(fileList);
        this.previewUrl.set(defaultPreviewUrl);
        return true;
      }),
      catchError(() => of(false))
    );

  };
}
