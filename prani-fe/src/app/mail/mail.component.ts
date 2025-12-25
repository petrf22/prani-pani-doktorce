import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { UserService } from '../services/user-service';
import { JsonPipe } from '@angular/common';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzImageModule } from 'ng-zorro-antd/image';
import { NzResultModule } from 'ng-zorro-antd/result';

@Component({
  selector: 'app-mail',
  imports: [FormsModule, NzFormModule, NzInputModule, NzInputModule, NzImageModule, NzAlertModule, NzButtonModule, NzResultModule, JsonPipe],
  templateUrl: './mail.component.html',
  styleUrls: ['./mail.component.css']
})
export class MailComponent {
  private userService = inject(UserService);

  email = '';
  sentInfo = signal<{message: string, error: boolean, sent: boolean}>({message: '', error: false, sent: false});

  submitForm(): void {
    console.log('MailComponent :: submit :: email:', this.email);

    this.userService.mailToken(this.email).subscribe({
      next: (response) => {
        console.log('MailComponent :: Mail token sent successfully:', response);

        this.sentInfo.set({message: 'E-mail byl úspěšně odeslán.', error: false, sent: true});
      },
      error: (error) => {
        console.error('MailComponent :: Error sending mail token:', error);
        this.sentInfo.set({message: 'Chyba při odesílání e-mailu.', error: true, sent: true});
      }
    });
  }
}
