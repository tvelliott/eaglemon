//MIT License
//
//Copyright (c) 2022 tvelliott

//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in all
//copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//SOFTWARE.

import java.nio.*;
import javax.sound.sampled.*;


class audio
{

  java.util.Timer utimer;
  volatile int do_new_audio = 0;
  byte[] outbytes = null;
  int audio_len;

  private int demod = 0;

  final int BUFFER_LEN = 12000; //good values are between 8000 and 12000


  byte[] byte_buffer = null;
  int out_s;
  int out_e;

  byte[] b2;
  int b2_idx = 0;

  int out_idx = 0;
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  class audio_thread extends java.util.TimerTask
  {

    public void run()
    {
      try {
        while( true ) {
          if( out_s != out_e ) {

            b2[b2_idx++] = byte_buffer[out_s++];
            out_s &= ( out_s & 0xffff );

            if( b2_idx == 2 ) {
              b2_idx = 0;

              outbytes[out_idx + 0] = b2[0];
              outbytes[out_idx + 1] = b2[1];

              outbytes[out_idx + 2] = b2[0];
              outbytes[out_idx + 3] = b2[1];

              out_idx += 4;
            }

            if( out_idx == 320 * 4 ) {
              try {
                sourceDataLine.write( outbytes, 0, out_idx );

                if( audio_tick_cnt < 30 ) {
                  sourceDataLine.start();
                  audio_tick_cnt = 30;
                }
              } catch( Exception e ) {
              }

              out_idx = 0;
              do_new_audio = 0;
              if( audio_active < 30 ) audio_active++;
            }
          } else if( out_s == out_e && sourceDataLine.available() > BUFFER_LEN / 2 ) {

            try {
              int idx = 0;
              for( int i = 0; i < 320; i++ ) {

                outbytes[idx + 0] = 0;
                outbytes[idx + 1] = 0;

                outbytes[idx + 2] = outbytes[idx + 0];
                outbytes[idx + 3] = outbytes[idx + 1];

                idx += 4;
              }
              sourceDataLine.write( outbytes, 0, idx );

              sourceDataLine.start();
              audio_tick_cnt = 30;
              if( audio_active > 0 ) audio_active--;
            } catch( Exception e ) {
            }

            //System.out.println("avail: "+sourceDataLine.available());
          }
          if( out_s == out_e ) Thread.sleep( 0, 100 );
        }

      } catch( Exception e ) {
        //e.printStackTrace();
      }
    }
  }

  public int audio_active = 0;
  public int audio_tick_cnt = 0;
  int audio_buf_cnt = 0;
  AudioFormat format;
  SourceDataLine sourceDataLine;

  ///////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////
  public audio()
  {
    format = new AudioFormat( 8000, 16, 2, true, false ); //last boolean is endian-type (false=little)
    try {
      sourceDataLine = AudioSystem.getSourceDataLine( format );
      sourceDataLine.open( format, BUFFER_LEN );
    } catch( Exception e ) {
      e.printStackTrace();
    }

    try {
      utimer = new java.util.Timer();
      utimer.schedule( new audio_thread(), 100, 1 );
    } catch( Exception e ) {
      e.printStackTrace();
    }

    if( outbytes == null ) outbytes = new byte[ 320 * 4 ];
    byte_buffer = new byte[65536];
    b2 = new byte[2];
  }

  ///////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////
  public void audio_tick()
  {
    if( audio_tick_cnt > 0 ) {
      audio_tick_cnt--;
      if( audio_tick_cnt == 0 ) {
        audio_buf_cnt = 0;
      }
    }
    if( audio_buf_cnt == 0 && sourceDataLine.available() == BUFFER_LEN ) sourceDataLine.stop();
  }
  ///////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////
  void play_audio( byte[] b, int len, int demod_mode )
  {

    for( int i = 0; i < len; i++ ) {
      byte_buffer[out_e++] = b[i];
      out_e = ( out_e & 0xffff );
    }

    demod = demod_mode;

  }
}
