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
  selector: 'app-mail-form',
  imports: [FormsModule, NzFormModule, NzInputModule, NzInputModule, NzImageModule, NzAlertModule, NzButtonModule, NzResultModule, JsonPipe],
  templateUrl: './mail-form.component.html',
  styleUrls: ['./mail-form.component.css']
})
export class MailFormComponent {
  private userService = inject(UserService);

  email = '';
  sentInfo = signal('');

  submitForm(): void {
    console.log('MailFormComponent :: submit :: email:', this.email);

    this.userService.mailToken(this.email).subscribe({
      next: (response) => {
        console.log('MailFormComponent :: Mail token sent successfully:', response);
        this.sentInfo.set('E-mail byl úspěšně odeslán.');
      },
      error: (error) => {
        console.error('MailFormComponent :: Error sending mail token:', error);
        this.sentInfo.set('Chyba při odesílání e-mailu.');
      }
    });
  }
}
